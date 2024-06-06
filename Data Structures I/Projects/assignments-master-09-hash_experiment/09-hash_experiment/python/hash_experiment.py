#!/usr/bin/env python3

import random, sys
from math import sqrt

# Our wrapper of random so we can substitute it with another random generator
rng_init = lambda x: random.seed(x)
rng_next_u32 = lambda: random.randint(0, 2**32 - 1)

class TabulationHash:
    """Hash function for hashing by tabulation.

    The 32-bit key is split to four 8-bit parts. Each part indexes
    a separate table of 256 randomly generated values. Obtained values
    are XORed together.
    """

    def __init__(self, num_buckets):
        self.num_buckets = num_buckets
        self.tables = [None] * 4
        for i in range(4):
            self.tables[i] = [ rng_next_u32() for _ in range(256) ]

    def __call__(self, key):
        h0 = key & 0xff;
        h1 = (key >> 8) & 0xff;
        h2 = (key >> 16) & 0xff;
        h3 = (key >> 24) & 0xff;
        t = self.tables
        return (t[0][h0] ^ t[1][h1] ^ t[2][h2] ^ t[3][h3]) % self.num_buckets

class PolynomialHash:
    """Hash function using polynomial modulo a prime."""

    def __init__(self, num_buckets, degree, prime = 2147483647):
        self.num_buckets = num_buckets
        self.prime = prime
        self.coefs = [ rng_next_u32() for _ in range(degree + 1) ]

    def __call__(self, key):
        acc = 0
        for c in self.coefs:
            acc = (acc * key + c) % self.prime
        return acc % self.num_buckets

LinearHash = lambda num_buckets: PolynomialHash(num_buckets, 1)
QuadraticHash = lambda num_buckets: PolynomialHash(num_buckets, 2)

class MultiplyShiftLowHash:
    """Multiply-shift hash function taking top bits of 32-bit word"""

    def __init__(self, num_buckets):
        self.mask = num_buckets - 1
        assert (num_buckets & self.mask == 0), \
            "MultiplyShiftLowHash: num_buckets must be power of 2"

        self.mult = rng_next_u32() | 0x1
        self.shift = 0;
        tmp = num_buckets - 1
        while 0x80000000 & tmp == 0:
            tmp <<= 1
            self.shift += 1

    def __call__(self, key):
        return ((key * self.mult) >> self.shift) & self.mask

class MultiplyShiftHighHash:
    """Multiply-shift hash function taking low bits of upper half of 64-bit word"""

    def __init__(self, num_buckets):
        self.mask = num_buckets - 1
        assert (num_buckets & self.mask == 0), \
            "MultiplyShiftLowHash: num_buckets must be power of 2"
        self.mult = (rng_next_u32() << 32) | rng_next_u32() | 0x1

    def __call__(self, key):
        return ((key * self.mult) >> 32) & self.mask

class HashTable:
    """Hash table with linear probing"""

    def __init__(self, hash_fun_factory, num_buckets):
        self._hash = hash_fun_factory(num_buckets)
        self._num_buckets = num_buckets
        self._table = [None] * num_buckets
        self._size = 0
        self.reset_counter()

    def _next_bucket(self, b):
        return (b + 1) % self._num_buckets

    def lookup(self, key):
        """Check whether key is present in the table."""
        ret = False
        steps = 1

        b = self._hash(key)
        while self._table[b] is not None:
            if self._table[b] == key:
              ret = True
              break
            steps += 1
            b = self._next_bucket(b)

        self._update_counter(steps)
        return ret

    def insert(self, key):
        """Add the key in the table."""
        assert self._size < self._num_buckets, "Cannot insert into a full table."
        steps = 1

        b = self._hash(key)
        while self._table[b] is not None:
            if self._table[b] == key: break
            steps += 1
            b = self._next_bucket(b)
        else:
            self._table[b] = key

        self._update_counter(steps)

    def _update_counter(self, steps):
        self._ops += 1
        self._steps += steps
        self._max = max(self._max, steps)

    def reset_counter(self):
        self._steps = 0
        self._ops = 0
        self._max = 0

    def report_avg(self): return self._steps / max(1, self._ops)
    def report_max(self): return self._max

def permute_list(l):
    N = len(l)
    for i in range(N - 1):
        dst = i + (rng_next_u32() % (N-i))
        l[i], l[dst] = l[dst], l[i]

def usage_test(hash_fun_factory, max_usage = 90, retry = 40):
    avg = [0.0] * max_usage
    avg2 = [0.0] * max_usage

    N = 2**19
    step_size = N // 100
    elements = list(range(N))

    for _ in range(retry):
        H = HashTable(hash_fun_factory, N)
        permute_list(elements)

        for s in range(max_usage):
            H.reset_counter()
            for i in range(step_size):
                H.insert(s*step_size + i)
            avg[s] += H.report_avg()
            avg2[s] += H.report_avg() ** 2


    NS, AVGS, STDEVS = [], [], []
    for i in range(max_usage):
        avg[i] /= retry;
        avg2[i] /= retry;
        std_dev = sqrt(avg2[i] - avg[i]**2)

        NS.append(i+1)
        AVGS.append(avg[i])
        STDEVS.append(std_dev)
        print("%i %.03f %.03f" % ((i + 1), avg[i], std_dev))
    print("NS:", NS)
    print("Averages:", AVGS)
    print("Std_devs:", STDEVS)

def grow_test(hash_fun_factory, usage = 60, retry = 40, begin = 7, end = 21):

    NS, AVGS, STDEVS = [], [], []
    for n in range(begin, end):
        avg = 0.0
        avg2 = 0.0
        N = 2 ** n
        elements = list(range(N))

        for _ in range(retry):
            H = HashTable(hash_fun_factory, N)
            permute_list(elements)

            for x in elements[:N * usage // 100]:
                H.insert(x)

            for i in range(N):
                H.lookup(i)

            avg += H.report_avg()
            avg2 += H.report_avg() ** 2

        avg /= retry
        avg2 /= retry
        std_dev = sqrt(avg2 - avg**2)

        NS.append(N)
        AVGS.append(avg)
        STDEVS.append(std_dev)
        print("%i %.03f %.03f" % (N, avg, std_dev))
    print("NS:", NS)
    print("Averages:", AVGS)
    print("Std_devs:", STDEVS)

tests = {
    "usage-ms-low": lambda: usage_test(MultiplyShiftLowHash),
    "usage-ms-high": lambda: usage_test(MultiplyShiftHighHash),
    "usage-poly-1": lambda: usage_test(LinearHash),
    "usage-poly-2": lambda: usage_test(QuadraticHash),
    "usage-tab": lambda: usage_test(TabulationHash),

    "grow-ms-low": lambda: grow_test(MultiplyShiftLowHash),
    "grow-ms-high": lambda: grow_test(MultiplyShiftHighHash),
    "grow-poly-1": lambda: grow_test(LinearHash),
    "grow-poly-2": lambda: grow_test(QuadraticHash),
    "grow-tab": lambda: grow_test(TabulationHash),
}

GROW = "grow-"
USAGE = "usage-"
MSL = "ms-low"
MSH = "ms-high"
P1 = "poly-1"
P2 = "poly-2"
TAB = "tab"

# if len(sys.argv) == 3:
ttype = USAGE
hmethod = MSH

print(ttype + hmethod + " test...")
test = ttype + hmethod
student_id = 73
rng_init(int(student_id))
if test in tests:
    tests[test]()
else:
    raise ValueError("Unknown test {}".format(test))
# else:
#     raise ValueError("Usage: {} <test> <student-id>".format(sys.argv[0]))
#
