#!/usr/bin/env python3
import math
import sys

from matrix_tests import TestMatrix
from matrix_transpose import Matrix

def generic_test(N, M, B, max_ratio, debug_level):
    m = TestMatrix(N, M, B, debug_level)
    #m = Matrix(N)
    m.fill_matrix()
    m.reset_stats()
    m.transpose()

    print("\t{} misses in {} accesses".format(m.stat_cache_misses, m.stat_accesses))
    if m.stat_accesses:
        mpa = m.stat_cache_misses / m.stat_accesses
        lb = 1 / B
        ratio = mpa / lb
        print("\t{:.6f} misses/access (lower bound is {:.6f} => ratio {:.6f}, need {:.6f})".format(mpa, lb, ratio, max_ratio))
        assert ratio <= max_ratio, "Algorithm did too many I/O operations."

    m.check_result()

# A list of all tests
tests = [
    # name                                  N      M     B  max_ratio  debug_level
    ("small2k",     lambda: generic_test(   4,    32,    8,         8,     2 )),
    ("small",       lambda: generic_test(  16,    64,    8,         4,     2 )),
    ("n100b16",     lambda: generic_test( 100,  1024,   16,         3,     1 )),
    ("n1000b16",    lambda: generic_test(1000,  1024,   16,         3,     1 )),
    ("n1000b64",    lambda: generic_test(1000,  8192,   64,         3,     1 )),
    ("n1000b256",   lambda: generic_test(1000, 65536,  256,         5,     1 )),
    ("n1000b4096",  lambda: generic_test(1000, 65536, 4096,        50,     1 )),
]

if __name__ == "__main__":
    for required_test in sys.argv[1:] or [name for name, _ in tests]:
        for name, test in tests:
            if name == required_test:
                print("Running test {}".format(name), file=sys.stderr)
                test()
                break
        else:
            raise ValueError("Unknown test {}".format(name))
