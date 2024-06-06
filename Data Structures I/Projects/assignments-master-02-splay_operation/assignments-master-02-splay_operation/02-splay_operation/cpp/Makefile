test: splay_operation_test
	./$<

CXXFLAGS=-std=c++11 -O2 -Wall -Wextra -g -Wno-sign-compare

splay_operation_test: splay_operation.h splay_operation_test.cpp test_main.cpp
	$(CXX) $(CXXFLAGS) $^ -o $@

clean::
	rm -f splay_operation_test

.PHONY: clean test
