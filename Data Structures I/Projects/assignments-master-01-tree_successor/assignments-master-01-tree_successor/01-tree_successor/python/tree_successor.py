#!/usr/bin/env python3

class Node:
    """Node in a binary tree `Tree`"""

    def __init__(self, key, left=None, right=None, parent=None):
        self.key = key
        self.left = left
        self.right = right
        self.parent = parent

    def printNode(self):
        if self is not None:
            print("key ", self.key, "; parent: ", self.parent, "; left ", self.left, "; right ", self.right)


class Tree:
    """A simple binary search tree"""

    def __init__(self, root=None):
        self.root = root

    def insert(self, key):
        """Insert key into the tree.

        If the key is already present, do nothing.
        """
        if self.root is None:
            self.root = Node(key)
            return

        node = self.root
        while node.key != key:
            if key < node.key:
                if node.left is None:
                    node.left = Node(key, parent=node)
                node = node.left
            else:
                if node.right is None:
                    node.right = Node(key, parent=node)
                node = node.right

    def successor(self, node=None):

        """Return successor of the given node.

                The successor of a node is the node with the next greater key.
                Return None if there is no such node.
                If the argument is None, return the node with the smallest key.
        """

        # Find successor
        if node is not None:
            # print("\nNew node and root")
            # self.root.printNode()
            # node.printNode()

            # Backtrack if no right child
            if node.right is None:
                relative = node.parent

                # Root of tree is max
                if relative is None:
                    return None
                else:
                    # Backtrack to root
                    while relative.parent is not None:
                        if relative.key > node.key:
                            return relative
                        else:
                            relative = relative.parent
                    else:
                        if relative.key > node.key:
                            # Relative is successor
                            return relative
                        else:
                            # Node with no successor
                            return None

            # Find leftmost after moving right
            else:
                return leftmost(node.right)

        else:
            # Find min of tree
            node = self.root
            return leftmost(node)


def leftmost(node):
    while node.left is not None:
        node = node.left

    return node
