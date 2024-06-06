STUDENT_ID ?= PLEASE_SET_STUDENT_ID

HASHFUNCS=ms-low ms-high poly-1 poly-2 tab

.PHONY: test
test: $(addprefix out/t-grow-, $(HASHFUNCS)) $(addprefix out/t-usage-, $(HASHFUNCS))

out/t-%: hash_experiment.py
	@mkdir -p out
	./hash_experiment.py $* $(STUDENT_ID) >$@

.PHONY: clean
clean:
	rm -rf out
