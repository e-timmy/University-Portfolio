#!/usr/bin/env python3
import sys

from matrix_tests import TestMatrix

def simulated_test(M, B, naive):
    ns = []
    misses = []
    for e in range(10, 25):
        N = int(2 ** (e/2))
        #print("    ", N, M, B, file=sys.stderr)
        m = TestMatrix(N, M, B, 0)
        m.fill_matrix()
        m.reset_stats()
        if naive:
            m.naive_transpose()
        else:
            m.transpose()
        misses_per_item = m.stat_cache_misses / (N*(N-1))
        print(N, misses_per_item, flush=True)
        ns.append(N)
        misses.append(misses_per_item)
        m.check_result()
    print("Ns:", ns)
    print("Misses:", misses)

tests = {
#                                                M     B
    "m1024-b16":    lambda n: simulated_test( 1024,   16, n),
    "m8192-b64":    lambda n: simulated_test( 8192,   64, n),
    "m65536-b256":  lambda n: simulated_test(65536,  256, n),
    "m65536-b4096": lambda n: simulated_test(65536, 4096, n),
}

#if len(sys.argv) == 3:
if True:
    #test = sys.argv[1]
    test = "m8192-b64"
    implementation = "smart"
    print("\nTest:", test, implementation, "\n")
    if implementation == "smart":
        naive = False
    elif implementation == "naive":
        naive = True
    else:
        raise ValueError("Last argument must be either 'smart' or 'naive'")
    if test in tests:
        tests[test](naive)
    else:
        raise ValueError("Unknown test {}".format(test))
else:
    raise ValueError("Usage: {} <test> (smart|naive)".format(sys.argv[0]))
