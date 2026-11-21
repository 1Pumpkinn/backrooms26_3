"""Generates the cell templates used by the Level 0 structure.

Run from the repository root:

    pip install nbtlib
    python tools/generate_templates.py

It writes the .nbt templates to src/main/resources/data/backrooms26_3/structure/level_0 and the
matching template pool to src/main/resources/data/backrooms26_3/worldgen/template_pool/level_0.

How the world is tiled
----------------------
The structure set places one structure on every second chunk (spacing 2, separation 1 means the
random offset is always 0), so a start lands every 32 blocks in both directions. Each start is a
single 33 x 5 x 33 cell. The size is odd on purpose:

* jigsaw centres the start piece on its origin and rotates it randomly, and an odd size keeps that
  centre exact for every rotation (an even size is off by one block for some rotations);
* 33 on a 32 block pitch means the outer wall row of a cell is the same row of blocks as the outer
  wall row of its neighbour, so there is exactly one wall between two cells and never a gap.

Because the cells are placed independently, a doorway only works when both neighbours leave it open
(a template only places the blocks it lists, so a wall from either side stays a wall). The door slots
are therefore at fixed, rotation symmetric positions, and each cell leaves each slot open with a
random chance. Inside a cell the layout is a recursive subdivision into small rooms (about 4 to 9
blocks across) joined by 3 wide gaps, so the space stays tight and every cell is connected inside.

Local y 0 to 3 is the wall space (world y 2 to 5; the structure json uses start_height 3 because jigsaw places the piece one block lower) and local y 4 is the ceiling row (world y 6) where
the light panels replace ceiling tiles.
"""
import json
import os
import random
import sys
from collections import deque

from nbtlib import Compound, File, Int, List, String

MOD = "backrooms26_3"
DATA_VERSION = 5023
ROOT = os.path.join("src", "main", "resources", "data", MOD)
STRUCTURE_DIR = os.path.join(ROOT, "structure", "level_0")
POOL_DIR = os.path.join(ROOT, "worldgen", "template_pool", "level_0")

N = 33
LAST = N - 1
SIZE = (N, 5, N)
WALL_HEIGHT = 4
DOOR_HEIGHT = 3
VARIANTS = 64

# Doorway slots on every outer edge, mirrored around the centre so they survive rotation.
DOOR_SLOTS = [range(6, 10), range(23, 27)]
DOOR_OPEN_CHANCE = 0.85

MIN_ROOM = 4
MAX_ROOM = 9
SPLIT_AFTER_SMALL = 0.35
GAP_WIDTH = 3
GAP_LINTEL_CHANCE = 0.7

LIGHT_X = (3, 11, 19, 27)
LIGHT_Z = (2, 10, 18, 26)
LIGHT_CHANCE = 0.75

WALLPAPER = 0
LIGHT = 1

PALETTE = [
    Compound({"id": String(f"{MOD}:yellow_wallpaper")}),
    Compound({"id": String(f"{MOD}:fluorescent_light")}),
]


def neighbours(x, z):
    yield x + 1, z
    yield x - 1, z
    yield x, z + 1
    yield x, z - 1


def connected(free):
    """True when every free cell can be reached from every other one."""
    if not free:
        return False
    start = next(iter(free))
    seen = {start}
    queue = deque([start])
    while queue:
        cell = queue.popleft()
        for nxt in neighbours(*cell):
            if nxt in free and nxt not in seen:
                seen.add(nxt)
                queue.append(nxt)
    return len(seen) == len(free)


def build_layout(rng):
    """Returns (walls, lintels, rooms, open_doors).

    walls    cells that are solid for the full wall height
    lintels  cells that only have the top block of the wall (doorway gaps)
    rooms    list of (x0, x1, z0, z1) leaf rooms, used to place pillars
    open_doors  list of (side, slot index) left open on the outer wall
    """
    walls = set()
    lintels = set()
    rooms = []

    def divide(x0, x1, z0, z1):
        w = x1 - x0 + 1
        h = z1 - z0 + 1
        can_x = w >= 2 * MIN_ROOM + 1
        can_z = h >= 2 * MIN_ROOM + 1
        small = w <= MAX_ROOM and h <= MAX_ROOM
        if not (can_x or can_z) or (small and rng.random() > SPLIT_AFTER_SMALL):
            rooms.append((x0, x1, z0, z1))
            return
        if can_x and can_z:
            vertical = w > h or (w == h and rng.random() < 0.5)
        else:
            vertical = can_x
        gap_lintel = rng.random() < GAP_LINTEL_CHANCE
        if vertical:
            at = rng.randint(x0 + MIN_ROOM, x1 - MIN_ROOM)
            gap = rng.randint(z0, z1 - GAP_WIDTH + 1)
            for z in range(z0, z1 + 1):
                if gap <= z < gap + GAP_WIDTH:
                    if gap_lintel:
                        lintels.add((at, z))
                else:
                    walls.add((at, z))
            divide(x0, at - 1, z0, z1)
            divide(at + 1, x1, z0, z1)
        else:
            at = rng.randint(z0 + MIN_ROOM, z1 - MIN_ROOM)
            gap = rng.randint(x0, x1 - GAP_WIDTH + 1)
            for x in range(x0, x1 + 1):
                if gap <= x < gap + GAP_WIDTH:
                    if gap_lintel:
                        lintels.add((x, at))
                else:
                    walls.add((x, at))
            divide(x0, x1, z0, at - 1)
            divide(x0, x1, at + 1, z1)

    divide(1, LAST - 1, 1, LAST - 1)

    open_doors = [(side, i) for side in "wnes" for i in range(len(DOOR_SLOTS)) if rng.random() < DOOR_OPEN_CHANCE]

    # Keep a small pocket free in front of every open doorway so no inner wall blocks it.
    for side, i in open_doors:
        for along in range(DOOR_SLOTS[i].start - 1, DOOR_SLOTS[i].stop + 1):
            for depth in (1, 2):
                cell = {"w": (depth, along), "e": (LAST - depth, along),
                        "n": (along, depth), "s": (along, LAST - depth)}[side]
                walls.discard(cell)
                lintels.discard(cell)

    return walls, lintels, rooms, open_doors


def free_cells(walls):
    return {(x, z) for x in range(1, LAST) for z in range(1, LAST) if (x, z) not in walls}


def add_pillars(walls, rooms, rng):
    for (x0, x1, z0, z1) in rooms:
        if x1 - x0 + 1 < 7 or z1 - z0 + 1 < 7 or rng.random() > 0.5:
            continue
        px = rng.randint(x0 + 2, x1 - 3)
        pz = rng.randint(z0 + 2, z1 - 3)
        cells = {(px + dx, pz + dz) for dx in range(2) for dz in range(2)}
        if cells & walls:
            continue
        trial = walls | cells
        if connected(free_cells(trial)):
            walls |= cells
    return walls


def build_room(rng):
    for _ in range(200):
        walls, lintels, rooms, open_doors = build_layout(rng)
        if connected(free_cells(walls)):
            break
    else:
        raise RuntimeError("could not build a connected layout")
    walls = add_pillars(walls, rooms, rng)

    blocks = {}

    def solid(x, z, y_from=0):
        for y in range(y_from, WALL_HEIGHT):
            blocks[(x, y, z)] = WALLPAPER

    # outer walls with doorways
    door_cells = {"w": set(), "e": set(), "n": set(), "s": set()}
    for side, i in open_doors:
        door_cells[side].update(DOOR_SLOTS[i])
    for i in range(N):
        solid(0, i, DOOR_HEIGHT if i in door_cells["w"] else 0)
        solid(LAST, i, DOOR_HEIGHT if i in door_cells["e"] else 0)
        solid(i, 0, DOOR_HEIGHT if i in door_cells["n"] else 0)
        solid(i, LAST, DOOR_HEIGHT if i in door_cells["s"] else 0)

    for (x, z) in walls:
        solid(x, z)
    for (x, z) in lintels:
        solid(x, z, DOOR_HEIGHT)

    # light panels on a regular lattice, some missing, some turned around
    for x0 in LIGHT_X:
        for z0 in LIGHT_Z:
            if rng.random() > LIGHT_CHANCE:
                continue
            width, depth = (2, 4) if rng.random() < 0.5 else (4, 2)
            for x in range(x0, x0 + width):
                for z in range(z0, z0 + depth):
                    blocks[(x, WALL_HEIGHT, z)] = LIGHT
    return blocks


def write_template(name, blocks):
    entries = []
    for (x, y, z), state in sorted(blocks.items()):
        entries.append(Compound({"pos": List[Int]([Int(x), Int(y), Int(z)]), "state": Int(state)}))
    root = Compound({
        "DataVersion": Int(DATA_VERSION),
        "size": List[Int]([Int(v) for v in SIZE]),
        "palette": List[Compound](PALETTE),
        "blocks": List[Compound](entries),
        "entities": List[Compound]([]),
    })
    File(root, gzipped=True).save(os.path.join(STRUCTURE_DIR, name + ".nbt"))


def pool_element(name, weight):
    return {
        "element": {
            "element_type": "minecraft:legacy_single_pool_element",
            "location": f"{MOD}:level_0/{name}",
            "processors": "minecraft:empty",
            "projection": "rigid",
        },
        "weight": weight,
    }


def write_pool(file_name, elements):
    with open(os.path.join(POOL_DIR, file_name + ".json"), "w", newline="\n") as f:
        json.dump({"elements": elements, "fallback": "minecraft:empty"}, f, indent="\t")
        f.write("\n")


def preview(blocks):
    for z in range(N):
        row = ""
        for x in range(N):
            if (x, 2, z) in blocks:
                row += "#"
            elif (x, 3, z) in blocks:
                row += "-"
            else:
                row += "."
        print(row)


def main():
    rng = random.Random(2603)
    if "--preview" in sys.argv:
        preview(build_room(rng))
        return
    os.makedirs(STRUCTURE_DIR, exist_ok=True)
    os.makedirs(POOL_DIR, exist_ok=True)
    for old in os.listdir(STRUCTURE_DIR):
        if old.endswith(".nbt"):
            os.remove(os.path.join(STRUCTURE_DIR, old))
    pool = []
    for i in range(VARIANTS):
        name = f"cell_{i:02d}"
        write_template(name, build_room(rng))
        pool.append(pool_element(name, 1))
    write_pool("start", pool)
    old_rooms = os.path.join(POOL_DIR, "rooms.json")
    if os.path.exists(old_rooms):
        os.remove(old_rooms)
    print(f"wrote {len(pool)} cells")


if __name__ == "__main__":
    main()
