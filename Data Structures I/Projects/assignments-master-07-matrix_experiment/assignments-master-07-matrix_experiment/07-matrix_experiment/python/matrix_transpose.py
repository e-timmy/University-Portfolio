
from math import ceil, floor

class Matrix:
    """Interface of a matrix.

    This class provides only the matrix size N and a method for swapping
    two items. The actual storage of the matrix in memory is provided by
    subclasses in testing code.
    """

    def __init__(self, N):
        self.N = N

    def swap(self, i1, j1, i2, j2):
        """Swap elements (i1,j1) and (i2,j2)."""

        # Overridden in subclasses
        raise NotImplementedError

    def transpose(self):
        """Transpose the matrix."""

        #print("transposing...", self.N)
        self.t_recurse(self.N, self.N, 0, 0)
        #print("finished transposing...")

    def t_recurse(self, N, P, i, j):

        if N == 1 and P == 1:
            if i < j:
                self.swap(i, j, j, i)
            return
        else:
            # Recursive case: break down into four quadrants
            if N >= P:
                # Case: split horizontally
                m1 = ceil(N / 2)
                m2 = floor(N / 2)
                #q = floor(P / 2)
                q = P

                # Left
                #print("Recursing left half. N:", N, "P:", P, "m:", m1, "q:", q, "(i, j):", (i, j))
                self.t_recurse(m1, q, i, j)
                # Right
                #print("Recursing right half. N:", N, "P:", P, "m:", m2, "q:", q, "(i, j):", (i + m1, j))
                self.t_recurse(m2, q, i + m1, j)
            else:
                # Case: split vertically
                m = N
                q1 = ceil(P / 2)
                q2 = floor(P / 2)

                # Top
                #print("Recursing top half. N:", N, "P:", P, "m:", m, "q:", q1, "(i, j):", (i, j))
                self.t_recurse(m, q1, i, j)
                # Bottom
                #print("Recursing bottom half. N:", N, "P:", P, "m:", m, "q:", q2, "(i, j):", (i, j + q2))
                self.t_recurse(m, q2, i, j + q1)