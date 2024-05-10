/**
 * @author Surya Venkatesh
 * @details Generates a valid incomplete nxn puzzle string that can be 
 *          copy/pasted into the terminal on Grok. Solution included. Only 
 *          works for board sizes up to and including (7x7).
 * 
 * @note compile program by running "gcc -o generator generator.c" if using gcc,
 *       or substitute gcc with another compiler. 
 *       Then execute the command "./generator N" on the terminal, where N is
 *       the desired board size.
 */

#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#define TRUE 1
#define FALSE 0

int** make_board(int size);
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
    
    if (size <= 2 || size > 7) {
        printf("Board size needs to be within [2..7], exiting...\n");
        exit(EXIT_FAILURE);
    }

    int** board = make_board(size);

    fill_board(board, size);
    print_board(board, size);
    free_board(board, size);

    return 0;
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
    for (int i = 0; i < size + 1; i++) {
        for (int j = 0; j < size + 1; j++) {
            board[i][j] = 0;
        }
    }

    return board;
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