#!/usr/bin/env python3
import math
import os
import string
import random
from hashlib import blake2s

# Test switches
T_ProcessData = False
T_INSERT = False
T_DUPLICATE = False

def find_duplicates(data_generator):
    """Find duplicates in the given data.

    The `data_generator` is an iterable over strings, so it can be
    iterated for example using a `for` cycle:

      for item in data_generator: ...

    It can be iterated multiple times.

    The goal is to return a list of duplicated entries, reporting each duplicated
    entry only once.
    """

    print("Creating bloom filter...")
    size = data_generator.length
    prob = 0.01
    bfilter = BloomFilter(size, prob)

    # Fill filters
    print("Retrieving candidates...")
    candidates = process_data(data_generator, bfilter)

    print("Finding duplicates...")
    duplicates = decode_duplicates(data_generator, candidates)

    return duplicates


def process_data(data_generator, bfilter):

    candidates = []
    for item in data_generator:
        collision = bfilter.insert(item)
        if collision:
            if T_ProcessData: print("Collision!", item)
            candidates.append([item, 0])

    return candidates


def decode_duplicates(data_generator, candidates):

    # Get real counts of candidates
    for item in data_generator:
        for c in candidates:
            if item == c[0]:
                c[1] += 1

    duplicates = []
    # Get duplicates
    for c in candidates:
        if c[1] > 1:
            if c[0] not in duplicates:
                duplicates.append(c[0])

    if T_DUPLICATE: print("Duplicates found:", duplicates)
    return duplicates


class BloomFilter:

    def __init__(self, n, p):

        # Bounds
        self.p = p
        self.n = n

        # Define filter size
        # Working from single table filter assumption
        self.k = math.ceil(math.log2(1/self.p))
        self.m = 2 * self.n * self.k
        self.barray = bytearray(self.m // 8)
        self.assign_hashes()

    def assign_hashes(self):
        self.hashes = []
        for i in range(self.k):
            salt = os.urandom(blake2s.SALT_SIZE)
            self.hashes.append(blake2s(salt=salt, usedforsecurity=False))
        print(self.hashes)

    def insert(self, item):

        collision = True

        for i in range(self.k):

            hash = self.hashes[i].copy()
            hash.update(item.encode())
            x = int(hash.hexdigest(), 16) % self.m

            byte, bit_index = divmod(x, 8)
            if not get_bit(self.barray[byte], bit_index):
                self.barray[byte] = toggle_bit(self.barray[byte], bit_index)
                collision = False

        return collision


def toggle_bit(byte, bit_index):
    return byte ^ (1 << bit_index)

def get_bit(byte, bit_index):
    return byte & (1 << bit_index)

