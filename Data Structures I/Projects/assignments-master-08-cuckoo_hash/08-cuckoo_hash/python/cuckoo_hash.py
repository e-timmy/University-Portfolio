import random
import math


class TabulationHash:
    """Hash function for hashing by tabulation.

    The 32-bit key is split to four 8-bit parts. Each part indexes
    a separate table of 256 randomly generated values. Obtained values
    are XORed together.
    """

    def __init__(self, num_buckets):
        self.tables = [None] * 4
        for i in range(4):
            self.tables[i] = [random.randint(0, 0xffffffff) for _ in range(256)]
        self.num_buckets = num_buckets

    def hash(self, key):
        h0 = key & 0xff
        h1 = (key >> 8) & 0xff
        h2 = (key >> 16) & 0xff
        h3 = (key >> 24) & 0xff
        t = self.tables
        return (t[0][h0] ^ t[1][h1] ^ t[2][h2] ^ t[3][h3]) % self.num_buckets


class CuckooTable:
    """Hash table with Cuckoo hashing.

    We have two hash functions, which map 32-bit keys to buckets of a common
    hash table. Unused buckets contain None.
    """

    def __init__(self, num_buckets):
        """Initialize the table with the given number of buckets.
        The number of buckets is expected to stay constant."""

        # The array of buckets
        self.num_buckets = num_buckets
        self.table = [None] * num_buckets

        # Create two fresh hash functions
        self.hashes = [TabulationHash(num_buckets), TabulationHash(num_buckets)]

    def lookup(self, key):
        """Check if the table contains the given key. Returns True or False."""

        b0 = self.hashes[0].hash(key)
        b1 = self.hashes[1].hash(key)
        # print("## Lookup key={} b0={} b1={}".format(key, b0, b1))
        return self.table[b0] == key or self.table[b1] == key

    def insert(self, key, rehash=False):
        """Insert a new key to the table. Assumes that the key is not present yet."""
        # Test switch
        T_Insert = False

        # Attempt insertion: returns if successful, otherwise cuckoo hash
        if self.hash_key(key):
            return True

        # Attempt cuckoo hash: returns none is successful, evicted key if not
        key = self.cuckoo_hash(key)
        if not key:
            return True

        # Control for recursive rehashings
        if rehash:
            return False

        # Rehash all keys given cuckoo failure
        if self.rehash(key):
            if T_Insert: print("Successful rehashing...")
            return True
        else:
            raise Exception("Failed to rehash keys within known bounds...")

    # Rehashes all keys in table (given previous cuckoo hash failure)
    # Returns success or failure (upon timeout)
    def rehash(self, key):
        # Test switch
        T_Rehash = False

        timeout = 6*math.log(self.num_buckets)
        rehashings = 0
        old_table = [key] + self.table

        while rehashings <= timeout:

            self.table = [None] * self.num_buckets
            self.hashes = [TabulationHash(self.num_buckets), TabulationHash(self.num_buckets)]
            outcome = True

            for key in old_table:
                if key is not None:
                    outcome = self.insert(key, rehash=True)
                    if not outcome:
                        if T_Rehash: print("Rehash -> failed to rehash... trying again")
                        break

            if outcome is True:
                break

            rehashings += 1

        # Control for timeout
        if rehashings > timeout:
            return False
        else:
            return True

    # Attempts replacement hash strategy evicting one key for new key
    # Returns success upon placement of all keys or after timeout
    def cuckoo_hash(self, key):
        # Test switch
        T_CuckooHash = False

        # Select random hash function to begin
        hash_index = random.randint(0, 1)
        hashings = 0
        timeout = 6*math.log(self.num_buckets)

        while hashings <= timeout:
            # Retrieve cuckooed key
            bucket = self.hashes[hash_index].hash(key)
            old_key = self.table[bucket]
            # Insert new key
            self.table[bucket] = key

            if T_CuckooHash: print("CuckooHash -> hash_index:", hash_index, "bucket:", bucket, "old_key:", old_key,
                                   "new_key:", key)

            # Control for looping replacement: get old_key's other hash_index
            if old_key is not None:
                b1 = self.hashes[0].hash(old_key)
                b2 = self.hashes[1].hash(old_key)
                if b1 == bucket:
                    hash_index = 1
                elif b2 == bucket:
                    hash_index = 0
                else:
                    raise Exception("CuckooHash -> bucket rehashing off...")
                key = old_key
            else:
                if T_CuckooHash: print("CuckooHash success...")
                return None
            hashings += 1

        return key

    # Attempts to hash key into table trying both hash functions
    # Returns success or failure
    def hash_key(self, key) -> bool:
        T_HashKey = False

        b1 = self.hashes[0].hash(key)
        b2 = self.hashes[1].hash(key)

        x1 = self.table[b1]
        x2 = self.table[b2]

        # Try first bucket
        if x1 is None:
            if T_HashKey: print("Open bucket:", b1, "Inserting key:", key)
            self.table[b1] = key
            return True
        else:
            if T_HashKey: print("Closed bucket:", b1, "key:", key, "different key present:", x1)

        # Try second bucket
        if x2 is None:
            if T_HashKey: print("Open bucket:", b2, "Inserting key:", key)
            self.table[b2] = key
            return True
        else:
            if T_HashKey: print("Closed bucket:", b2, "key:", key, "different key present:", x2)

        return False
