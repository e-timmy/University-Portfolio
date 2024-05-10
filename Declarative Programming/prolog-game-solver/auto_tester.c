/**
 * @author Surya Venkatesh
 * @details Automatically tests prolog program against sample puzzles.
 * 
 * @note Need swipl-ld installed. 
 *       compile program by running "swipl-ld -goal true -o auto_tester 
 *       auto_tester.c puzzle_solution.pl".
 *       Then execute the command "./auto_tester N S" on the terminal, where N is
 *       the desired board size and S is sleep time between rounds in seconds.
 * 
 * @credit https://stackoverflow.com/questions/65118493/is-there-any-reasonable-way-to-embed-a-prolog-interpreter-inside-of-a-c-program
 *         https://www.swi-prolog.org/pldoc/man?section=calling-prolog-from-c
 */

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <SWI-Prolog.h>
#include <unistd.h>

#define MAXLINE 1024

void get_board_string(int** board, int size, char* buf);
void get_query_board(int** board, int** query_board, int size);
void put_list(term_t l, int n, int* numbers);
void put_list_list(term_t l, int n, int len, int **numbers);
void execute_prolog(int size, int sleep_time);
int** make_board(int size);
void intialise_board(int** board, int size);
void print_board(int** board, int size);
int fill_board(int** board, int size);
int rand_val();
int rand_type();
int get_row_sum(int* array, int size);
int get_row_prod(int* array, int size);
int get_col_sum(int** board, int col, int size);
int get_col_prod(int** board, int col, int size);
void free_board(int** board, int size);

int main(int argc, char **argv) {
    srand(time(NULL));

    if (argc < 2) {
        printf("Board size was not input, exiting...\n");
        exit(EXIT_FAILURE);
    }

    int size = atoi(argv[1]);
    
    if (size < 2 || size > 5) {
        printf("Board size needs to be within [2..5], exiting...\n");
        exit(EXIT_FAILURE);
    }
    
    int sleep_time = 1;
    if (argc == 3) {
        sleep_time = atoi(argv[2]);
    }

    char *program = argv[0];
    char *plav[2];

    /* make the argument vector for Prolog */
    plav[0] = program;
    plav[1] = NULL;

    /* initialise Prolog */

    if (!PL_initialise(1, plav)) PL_halt(1);

    execute_prolog(size, sleep_time);

    return 0;
}

void execute_prolog(int size, int sleep_time) {
    int** board = make_board(size);
    int** query_board = make_board(size);
    char buf_sol[MAXLINE] = {0};
    char buf_test_case[MAXLINE] = {0};
    // print_board(board, size);
    
    // Buffer
    char buf[MAXLINE] = {0};
    int n = 0;
    int found = FALSE;
    int round = 0;

    predicate_t pred = PL_predicate("puzzle_solution", 1, "user");
    term_t a = PL_new_term_refs(1);

    while (TRUE) {
        found = FALSE;
        n = 0;
        memset(buf, 0, MAXLINE);

        intialise_board(board, size);
        intialise_board(query_board, size);
        fill_board(board, size);
        get_query_board(board, query_board, size);
        get_board_string(board, size, buf_sol);
        get_board_string(query_board, size, buf_test_case);

        
        printf("******************* ROUND %d *******************\n", ++round);
        printf("Test Case: %s\n", buf_test_case);
        printf("Target Solution: %s\n", buf_sol);
        printf("Your Solution(s): \n");

        put_list_list(a, size + 1, size + 1, query_board);

        qid_t qid = PL_open_query(NULL, PL_Q_NORMAL, pred, a);
        while (PL_next_solution(qid) == TRUE) {
            term_t tail = PL_copy_term_ref(a);
            term_t head = PL_new_term_ref();
            int x;

            n = snprintf(buf, MAXLINE, "[");
            while (PL_get_list(tail, head, tail)) {
                n += snprintf(buf + n, MAXLINE - n, "[");
                term_t head2 = PL_new_term_ref();
                while(PL_get_list(head, head2, head))
                {
                    if (PL_get_integer(head2, &x)) {
                        n += snprintf(buf + n, MAXLINE - n, "%d,", x);
                    } else {
                        printf("Error: could not get integer\n");
                        exit(EXIT_FAILURE);
                    }
                }
                n -= 1;
                n += snprintf(buf + n, MAXLINE - n, "],");
            }
            n -= 1;
            n += snprintf(buf + n, MAXLINE - n, "]");
            
            // printf("board_sol: %s\n", buf_sol);
            if (strcmp(buf, buf_sol) == 0) {
                found = TRUE;
                printf("* %s *\n\n", buf);
                break;
            }

            // Check solution
            printf("%s\n", buf);
        }

        PL_close_query(qid);

        if (found) {
            // printf("Solution found!\n\n");
            sleep(sleep_time);
        } else {
            printf("No solution found!\n\n");
            break;
        }
    }
    PL_halt(0);

    free_board(board, size);
    free_board(query_board, size);
}

void get_board_string(int** board, int size, char* buf) {
    int n = 0;
    int i, j;
    n = snprintf(buf, MAXLINE, "[");
    for (i = 0; i < size + 1; i++) {
        n += snprintf(buf + n, MAXLINE - n, "[");
        for (j = 0; j < size + 1; j++) {
            if (board[i][j] == -1) {
                n += snprintf(buf + n, MAXLINE - n, "_,");
                continue;
            }
            n += snprintf(buf + n, MAXLINE - n, "%d,", board[i][j]);
        }
        n -= 1;
        n += snprintf(buf + n, MAXLINE - n, "],");
    }
    n -= 1;
    n += snprintf(buf + n, MAXLINE - n, "]");
}

// Modified from:
// https://www.swi-prolog.org/pldoc/man?section=foreign-term-construct
void put_list(term_t l, int n, int *numbers)
{ term_t a = PL_new_term_ref();

  PL_put_nil(l);
  while( --n >= 0 )
  { 
    if (numbers[n] == -1) { 
        PL_put_variable(a);
    } else { 
        if (!PL_put_integer(a, numbers[n])) {
            printf("Error: could not put integer\n");
            exit(EXIT_FAILURE);
        }
    }
    if (!PL_cons_list(l, a, l)) {
        printf("Error: could not cons list\n");
        exit(EXIT_FAILURE);
    }
  }
}

void
put_list_list(term_t l, int n, int len, int **numbers)
{ term_t a = PL_new_term_ref();

  PL_put_nil(l);
  while( --n >= 0 )
  { 
    put_list(a, len, numbers[n]);
    if (!PL_cons_list(l, a, l)) {
        printf("Error: could not cons list\n");
        exit(EXIT_FAILURE);
    }
  }
}

// Create board size based off provided size
int** make_board(int size) {
    int** board = malloc(sizeof(int*) * (size + 1));
    if (board == NULL) {
        printf("ERROR: malloc failed.");
        exit(EXIT_FAILURE);
    }

    for (int i = 0; i < size + 1; i++) {
        board[i] = malloc(sizeof(int*) * (size + 1));
        if (board[i] == NULL) {
            printf("ERROR: malloc failed.");
            exit(EXIT_FAILURE);
        }
    }

    // Instantiate board values
    intialise_board(board, size);

    return board;
}

void intialise_board(int** board, int size) {
    for (int i = 0; i < size + 1; i++) {
        for (int j = 0; j < size + 1; j++) {
            board[i][j] = 0;
        }
    }
}

// Print board size including solution and grok input
void get_query_board(int** board, int** query_board, int size) {
    for (int i = 0; i < size + 1; i++) {
        for (int j = 0; j < size + 1; j++) {
            if (i == 0 && j == size) {
                query_board[i][j] = board[i][j];
                continue;
            }
            else if (i == 0 || j == 0) {
                query_board[i][j] = board[i][j];
                continue;
            }
            else {
                query_board[i][j] = -1;
            }
        }
    }
}

// Print board size including solution and grok input
void print_board(int** board, int size) {
    // Solution
    printf("Solution: [");
    for (int i = 0; i < size + 1; i++) {
        printf("[");
        for (int j = 0; j < size + 1; j++) {
            if (j == size) {
                printf("%d", board[i][j]);
                continue;
            }
            printf("%d,", board[i][j]);
        }
        if (i == size) {
            printf("]");
            continue;
        }
        printf("],");
    }
    printf("]\n");

    // Grok terminal input
    printf("Puzzle=");
    printf("[");
    for (int i = 0; i < size + 1; i++) {
        printf("[");
        for (int j = 0; j < size + 1; j++) {
            if (i == 0 && j == size) {
                printf("%d", board[i][j]);
            }
            else if (i == 0 || j == 0) printf("%d,", board[i][j]);
            else {
                if (j == size) {
                    printf("_");
                    continue;
                }
                printf("_,");
            }
        }
        if (i == size) {
            printf("]");
            continue;
        }
        printf("],");
    }
    printf("]");
    printf(", puzzle_solution(Puzzle).\n");
}

// Fill in board according to specifications
int fill_board(int** board, int size) {
    // FIll diagonal
    int diag = rand_val();
    for (int i = 1; i < size + 1; i++) {
        board[i][i] = diag;
    }

    int val = 0;
    int dup = FALSE;

    // Fill rest of board
    for (int i = 1; i < size + 1; i++) {
        for (int j = 1; j < size + 1; j++) {
            if (i == j) continue;
            while (1) {
                if ((val = rand_val()) == diag) continue;
                dup = FALSE;
                for (int s = 1; s < size + 1; s++) {
                    if (board[i][s] == val 
                    || board[s][j] == val) {
                        dup = TRUE;
                        break;
                    }
                }
                if (dup) continue;
                break;
            }
            board[i][j] = val;
        }
    }

    // Fill in headers
    for (int i = 1; i < size + 1; i++) {
        if (rand_type()) {
            board[i][0] = get_row_sum(board[i], size);
        } else {
            board[i][0] = get_row_prod(board[i], size);
        }

        if (rand_type()) {
            board[0][i] = get_col_sum(board, i, size);
        } else {
            board[0][i] = get_col_prod(board, i, size);
        }
    }

}

int rand_val() {
    int val = rand() % 9 + 1;
    return val;
}

int rand_type() {
    return rand() % 2;
}

int get_row_sum(int* array, int size) {
    int sum = 0;
    for (int i = 1; i < size + 1; i++) {
        sum += array[i];
    }
    return sum;
}

int get_row_prod(int* array, int size) {
    int prod = 1;
    for (int i = 1; i < size + 1; i++) {
        prod *= array[i];
    }
    return prod;
}

int get_col_sum(int** board, int col, int size) {
    int sum = 0;
    for (int i = 1; i < size + 1; i++) {
        sum += board[i][col];
    }
    return sum;
}

int get_col_prod(int** board, int col, int size) {
    int prod = 1;
    for (int i = 1; i < size + 1; i++) {
        prod *= board[i][col];
    }
    return prod;
}

void free_board(int** board, int size) {
    for (int i = 0; i < size + 1; i++) {
        free(board[i]);
    }
    free(board);
}