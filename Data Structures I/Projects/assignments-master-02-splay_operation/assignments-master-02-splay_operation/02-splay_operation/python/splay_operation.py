#!/usr/bin/env python3

class Node:
    """Node in a binary tree `Tree`"""

    def __init__(self, key, left=None, right=None, parent=None):
        self.key = key
        self.parent = parent
        self.left = left
        self.right = right
        if left is not None: left.parent = self
        if right is not None: right.parent = self

class Tree:
    """A simple binary search tree"""

    def __init__(self, root=None):
        self.root = root

    def rotate(self, node):
        """ Rotate the given `node` up.

        Performs a single rotation of the edge between the given node
        and its parent, choosing left or right rotation appropriately.
        """
        if node.parent is not None:
            if node.parent.left == node:
                if node.right is not None: node.right.parent = node.parent
                node.parent.left = node.right
                node.right = node.parent
            else:
                if node.left is not None: node.left.parent = node.parent
                node.parent.right = node.left
                node.left = node.parent
            if node.parent.parent is not None:
                if node.parent.parent.left == node.parent:
                    node.parent.parent.left = node
                else:
                    node.parent.parent.right = node
            else:
                self.root = node
            node.parent.parent, node.parent = node, node.parent.parent

    def lookup(self, key):
        """Look up the given key in the tree.

        Returns the node with the requested key or `None`.
        """
        # TODO: Utilize splay suitably.
        node = self.root
        prev = node
        while node is not None:
            if node.key == key:
                self.splay(node)
                return node
            if key < node.key:
                prev = node
                node = node.left
            else:
                prev = node
                node = node.right

        self.splay(prev)

        return None

    def insert(self, key):
        """Insert key into the tree.

        If the key is already present, nothing happens.
        """
        # TODO: Utilize splay suitably.
        if self.root is None:
            self.root = Node(key)
            return

        node = self.root
        insert = False
        while node.key != key:
            if key < node.key:
                if node.left is None:
                    node.left = Node(key, parent=node)
                    insert = True
                node = node.left
            else:
                if node.right is None:
                    node.right = Node(key, parent=node)
                    insert = True
                node = node.right

        if insert:
            self.splay(node)

    def remove(self, key):
        """Remove given key from the tree.

        It the key is not present, nothing happens.
        """
        # TODO: Utilize splay suitably.
        node = self.root
        prev = node

        # Find node
        while node is not None and node.key != key:
            if key < node.key:
                prev = node
                node = node.left
            else:
                prev = node
                node = node.right

        # Remove node
        if node is not None:

            # Replacement if two children
            if node.left is not None and node.right is not None:
                replacement = node.right
                while replacement.left is not None:
                    replacement = replacement.left
                node.key = replacement.key
                node = replacement

            # Replacement if one child
            replacement = node.left if node.left is not None else node.right

            # Changing address of parent
            if node.parent is not None:
                if node.parent.left == node: node.parent.left = replacement
                else: node.parent.right = replacement
            else:
                self.root = replacement

            if replacement is not None:
                replacement.parent = node.parent
                self.splay(replacement.parent)
        else:
            self.splay(prev)

    def splay(self, node):
        """Splay the given node.

        If a single rotation needs to be performed, perform it as the last rotation
        (i.e., to move the splayed node to the root of the tree).
        """
        # TODO: Implement
        if node is None:
            return

        while node.parent is not None:

            parent = node.parent
            gparent = parent.parent

            # Final zig operation
            if gparent is None:
                #print("Finished")
                self.rotate(node)
                return

            # Zig-zig operation
            if node.key < parent.key < gparent.key:
                #print("Zig Zig Lesser")
                self.rotate(parent)
                self.rotate(node)

            elif node.key > parent.key > gparent.key:
                #print("Zig zig greater")
                self.rotate(parent)
                self.rotate(node)

            # Zig-zag operation
            elif parent.key > node.key > gparent.key:
                #print("Zig zag gparent small")
                self.rotate(node)
                self.rotate(node)

            elif parent.key < node.key < gparent.key:
                #print("Zig zag parent small")
                self.rotate(node)
                self.rotate(node)
            else:
                print("something wrong")

        return

