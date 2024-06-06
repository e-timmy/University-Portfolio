#!/usr/bin/env python3

class ABNode:
    """Single node in an ABTree.

    Each node contains keys and children
    (with one more children than there are keys).
    We also store a pointer to node's parent (None for root).
    """
    def __init__(self, keys = None, children = None, parent = None):
        self.keys = keys if keys is not None else []
        self.children = children if children is not None else []
        self.parent = parent

    def find_branch(self, key):
        """ Try finding given key in this node.

        If this node contains the given key, returns (True, key_position).
        If not, returns (False, first_position_with_key_greater_than_the_given).
        """
        i = 0
        while (i < len(self.keys) and self.keys[i] < key):
            i += 1

        return (i < len(self.keys) and self.keys[i] == key, i)

    def insert_branch(self, i, key, child):
        """ Insert a new key and a given child between keys i and i+1."""
        self.keys.insert(i, key)
        self.children.insert(i + 1, child)

    def print_children(self):
        children = self.children

        print("\tChildren: ")
        for child in children:
            if child is None:
                break
            print("\t\t", child.keys)

    def __str__(self):
        if self is None:
            return "None"
        else:
            return "[Node keys: " + str(self.keys) + "; parent: " + str(self.parent) + "; children: " + str(self.children)



class ABTree:
    """A class representing the whole ABTree."""
    def __init__(self, a, b):
        assert a >= 2 and b >= 2 * a - 1, "Invalid values of a, b: {}, {}".format(a, b)
        self.a = a
        self.b = b
        self.root = ABNode(children=[None])

    def find(self, key):
        """Find a key in the tree.

        Returns True if the key is present, False otherwise.
        """
        node = self.root
        while node:
            found, i = node.find_branch(key)
            if found: return True
            node = node.children[i]
        return False

    def split_node(self, node, size):
        """Helper function for insert

        Split node into two nodes such that original node contains first _size_ children.
        Return new node and the key separating nodes.
        """

        keys = node.keys
        children = node.children
        parent = node.parent
        middle = keys[size]

        # Alter previous node, create new node
        node.keys = keys[0:size]
        node.children = children[0:size+1]
        node2 = ABNode(keys=keys[size+1:], children=children[size+1:], parent=parent)

        # Adjust parents of children in new nodes
        for i in range(len(children)):
            if children[i] is None:
                break
            if i < size+1:
                children[i].parent = node
            else:
                children[i].parent = node2

        if parent is None:
            # Handle root case (two children)
            parent = ABNode(keys=[middle], children=[node, node2], parent=None)
            self.root = parent
        else:
            # Find place for key to move upward
            found, i = parent.find_branch(middle)
            if found:
                print("Problem... found key... shouldn't be present")
            else:
                parent.insert_branch(i, middle, node2)

        # Update parent for all children
        for child in parent.children:
            child.parent = parent

        return node2, parent.keys[0]


    def insert(self, key):
        """Add a given key to the tree, unless already present."""

        # Find most recent node
        node = self.root
        while node:
            found, i = node.find_branch(key)
            if found:
                # Case 1: Node found -> do nothing
                return
            parent = node
            node = node.children[i]

        # Case 2: no node, insert
        parent.insert_branch(i, key, node)

        if len(parent.children) <= self.b:
            # Case 2a: Room in leaves for node
            return
        else:
            # Case 2b: overflow, split node
            while len(parent.children) > self.b:
                parent, split_key = self.split_node(parent, self.b // 2)

                # Check whether need to split returned node
                if len(parent.keys) >= self.b:
                    continue
                else:
                    parent = parent.parent

                # Break if root
                if parent is None:
                    break
                # Break if node not overflowing
                elif self.b > len(parent.keys) >= self.a:
                    break