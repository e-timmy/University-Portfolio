
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

        print("transposing...", self.N)
        self.t_recurse(self.N, self.N, 0, 0)
        print("finished transposing...")

    def t_recurse(self, N, P, i, j):

        if N == 1 or P == 1:
            return
        else:
            # Recursive case: break down into four quadrants
            m = ceil(N / 2)
            q = floor(P / 2)

            #print("Recursing top left. N:", N, "P:", P, "m:", q, "q:", q, "(i, j):", (i, j))
            self.t_recurse(m, m, i, j)
            # Bottom right
            #print("Recursing bottom right. N:", N, "P:", P, "m:", q, "q:", q, "(i, j):", (i + m, j + m))
            self.t_recurse(m, m, i + q, j + q)

            #print("Recursing top right. N:", N, "P:", P, "m:", m, "q:", m, "(i, j):", (i + q, j))
            self.t_recurse(q, q, i + m, j)
            #print("Recursing bottom left. N:", N, "P:", P, "m:", m, "q:", m, "(i, j):", (i, j + q))
            self.t_recurse(q, q, i, j + m)

            #print("Swap transpose off diags. N:", N, "P:", P, "m:", m, "q:", q, "(i, j):", (i, j))
            for x in range(i + m, i + m + q):
                for y in range(j, j + q):
                    #print("(x, y):", (x, y))
                    self.swap(x, y, x - m, y + m)
