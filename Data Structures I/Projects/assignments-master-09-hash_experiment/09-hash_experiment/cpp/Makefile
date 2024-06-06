INCLUDE ?= .
CXXFLAGS=-std=c++11 -O2 -Wall -Wextra -g -Wno-sign-compare -I$(INCLUDE)
STUDENT_ID ?= PLEASE_SET_STUDENT_ID

HASHFUNCS=ms-low ms-high poly-1 poly-2 tab

.PHONY: test
test: $(addprefix out/t-grow-, $(HASHFUNCS)) $(addprefix out/t-usage-, $(HASHFUNCS))

out/t-%: hash_experiment
	@mkdir -p out
	./hash_experiment $* $(STUDENT_ID) >$@

hash_experiment: hash_experiment.cpp $(INCLUDE)/random.h
	$(CXX) $(CPPFLAGS) $(CXXFLAGS) hash_experiment.cpp -o $@

.PHONY: clean
clean:
	rm -f hash_experiment
	rm -rf out
