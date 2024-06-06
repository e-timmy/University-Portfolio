## Goal

The goal of this assignment is to experimentally evaluate Linear probing
hash table with different systems of hash functions.

You are given a test program (`hash_experiment`) which implements everything
needed to perform the following experiments:

- _Grow experiment:_ This experiment tries different sizes $N$ of the hash table and for each size
  it inserts small keys in random order until 60% of the table is used
  and then it performs lookup operation for keys $0,\ldots,N-1$.
- _Usage experiment:_ This experiment uses hash table of size $2^{20}$. It performs insertions
  to increase usage of the table by 1%, reports efficiency of the insert operation,
  and repeats until usage of the table reaches 90%.

Both experiments measure number of probed buckets per operation, are repeated 40 times
and report average and standard deviation. Note that even with 40 repetitions
the reported numbers still depend quite a lot on the random seed used.

You should perform these experiments for 5 different classes of hash functions –
tabulation, multiply-shift which uses top bits of 32-bit word (`ms-low`),
multiply-shift which uses low bits of upper half of 64-bit word (`ms-high`),
and polynomial hash function of degree 1 and 2 – and write a report, which contains two
plots of the measured data for each experiment. The first plot should contain average
complexity of operations and the second one the standard deviation.

Each plot should show the dependence of the average number of probed buckets
either on size of the hash table (the grow experiment) or the usage of the hash table
(the usage experiment).

The report should discuss the experimental results and try to explain the observed
behavior using theory from the lectures. (If you want, you can carry out further
experiments to gain better understanding of the data structure and include these
in the report. This is strictly optional.)

You should submit a PDF file with the report (and no source code).
You will get 1 temporary point upon submission if the file is syntactically correct;
proper points will be assigned later.

## Test program

The test program is given two arguments:
- The name of the test (`{grow,usage}-{ms-low,ms-high,poly-1,poly-2,tab}`).
- The random seed: you should use the last 2 digits of your student ID (you can find
  it in the Study Information System – just click on the Personal data icon). Please
  include the random seed in your report.

The output of the program contains one line per experiment, which consists of
the set size and the average number of structural changes.

## Hints

The following tools can be useful for producing nice plots:
- [pandas](https://pandas.pydata.org/)
- [matplotlib](https://matplotlib.org/)
- [gnuplot](http://www.gnuplot.info/)

A quick checklist for plots:
- Is there a caption explaining what is plotted?
- Are the axes clearly labelled? Do they have value ranges and units?
- Have you mentioned that this axis has logarithmic scale? (Logarithmic graphs
  are more fitting in some cases, but you should tell.)
- Is it clear which curve means what?
- Is it clear what are the measured points and what is an interpolated
  curve between them?
- Are there any overlaps? (E.g., the most interesting part of the curve
  hidden underneath a label?)

In your discussion, please distinguish the following kinds of claims.
It should be always clear which is which:
- Experimental results (i.e., the raw data you obtained from the experiments)
- Theoretical facts (i.e., claims we have proved mathematically)
- Your hypotheses (e.g., when you claim that the graph looks like something is true,
  but you are not able to prove rigorously that it always holds)

Source code templates can be found in [git](https://gitlab.kam.mff.cuni.cz/datovky/assignments/-/tree/master).
