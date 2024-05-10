%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Filename: proj1.pl
% Author: Timothy Holland <tholland1@student.unimelb.edu.au>
% Purpose: Program solves Maths Puzzle (MP) using CLP(FD).
% Maths puzzle description in four constraints:
%     - C1: Maths puzzle is square grid of squares, 
%       each filled with single digit (1-9).
%     - C2: Each row and column contains no repeated digits.
%     - C3: All squares on diagonal from upper left to lower right
%       contain the same value.
%     - C4: Heading of each row/column holds either
%       sum or product of all digits in that row/column.
% Solver Description: 
%     Outcomes of solver:
%         - return solution if given semi-bounded input; 
%         - return true/false if given bounded input; 
%         - return false if false input. 
%     The solver handles each MP constraint by breaking puzzle
%     into data structures which match the four constraints and applying the 
%     given rule: 
%         - find list of all puzzle elements (non-headers) and 
%         constrain domain between 1-9; 
%         - find list of diagonal elements and ensure identity
%         - find lists of row/column elements (without headers) 
%           and ensure difference
%         - find lists of rows/columns (with headers) and ensure
%           header is sum/product of elements. 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Libraries
:- ensure_loaded(library(clpfd)).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%% Main Predicate

%% puzzle_solution(+Puzzle) is nondet.
%  Solves by splitting puzzle into data structures and applying constraints.
%  @param Puzzle: NxN list of lists of 1-9 integers.
puzzle_solution([Header|Rows]) :-
    
    correct_input([Header|Rows]),
    
    % C1: Bind elements to domain.
    maplist(behead, Rows, _, RowVars),
    flatten(RowVars, FlatVars),
    FlatVars ins 1..9,
    
    % C3: Equate diagonals.
    diagonals(RowVars, Diagonals),
    all_same(Diagonals),
     
    % C2: Differentiate elements in lines.
    transpose([Header|Rows], [_|Columns]),
    append(Rows, Columns, Lines),
    maplist(behead, Lines, _, Vars),
    maplist(all_different, Vars),
    
    % C4: Compute mathematical operations on lines.
    maplist(legal_line, Lines),
    labeling([], FlatVars).
    
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%% Helper Predicates %%%

%% correct_input(++Input) is det.
%% correct_input(++Input, +N: int) is det.
%  Checks input is NxN list of lists of variables or ints.
%  Extra argument N checks length of outer list against inner.
correct_input([Head|Rows]) :-
    length([Head|Rows], N),
    Head = [_|Row1],
    NewHead = [0|Row1],
    correct_input([NewHead|Rows], N).
correct_input([], _).
correct_input([Header|Rows], N) :-
    var_int_list(Header),
    length(Header, N1),
    N1 = N,
    correct_input(Rows, N).

%% var_int_list(?List: list) is nondet.
%  Checks list composition of vars or ints.
var_int_list([]).
var_int_list([Elt|List]) :-
    (var(Elt) ; integer(Elt)),
    var_int_list(List).

%% all_same(?List: list) is nondet.
%  Checks uniform list.
all_same([]).
all_same([_]).
all_same([Elt|List]) :-
    List = [Next|_],
    Elt = Next,
    all_same(List).

%% diagonals(+Lists: list of lists, ?Diagonals: list) is det.
%% diagonals(+Lists, +N: int, +Acc: list, ?Diagonals) is det.
%  Derives diagonal list from list of lists.
diagonals(Lists, Diagonals) :-
    diagonals(Lists, 0, [], Diagonals).
diagonals([], _, Diagonals, Diagonals).
diagonals([Head|Tail], N, Acc, Diagonals) :-
    nth0(N, Head, D_Elt),
    N1 is N + 1,
    append(Acc, [D_Elt], Acc1),
    diagonals(Tail, N1, Acc1, Diagonals).

%% behead(?List, ?Elt, ?Tail) is det.
%  Splits list into head and tail.
behead([Head|Tail], Head, Tail).

%% legal_line(+List: list) is nondet.
%  Checks Head of list is addition or product of elements.
legal_line([Total|Vars]) :-
    (sum(Vars, #=, Total);
    product(Vars, Total)).

%% product(?List: list, ?Total: int) is nondet.
%% product(?List, +A: int, ?Total) is nondet.
%  Calculates cumulative product of list.
product(List, Total) :-
    product(List, 1, Total).
product([], Total, Total).
product([Elt|List], A, Total) :-
    A1 #= A*Elt,
    product(List, A1, Total).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%