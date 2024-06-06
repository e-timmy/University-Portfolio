test: tree_successor_test
	./$<

CXXFLAGS=-std=c++11 -O2 -Wall -Wextra -g -Wno-sign-compare

tree_successor_test: tree_successor.h tree_successor_test.cpp test_main.cpp
	$(CXX) $(CXXFLAGS) $^ -o $@

clean:
	rm -f tree_successor_test

.PHONY: clean test
