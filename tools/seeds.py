"""Prints the 300 seeds from seeds.txt.

Eight-digit numbers from a generator with a fixed seed, so that the list
looks like seeds from real CI logs and can be rebuilt byte for byte:

    python3 tools/seeds.py | cmp - seeds.txt
"""
import random

rng = random.Random(147)
for _ in range(300):
    print(rng.randrange(10_000_000, 100_000_000))
