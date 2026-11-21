"""Generates the jigsaw room templates used by the Level 0 structure.

Run from the repository root:

    pip install nbtlib
    python tools/generate_templates.py

It writes the .nbt templates to src/main/resources/data/backrooms26_3/structure/level_0 and the
matching template pools to src/main/resources/data/backrooms26_3/worldgen/template_pool/level_0.

Every room is a 16 x 5 x 16 tile. Local y 0 to 3 is the wall space (world y 2 to 5) and local y 4 is
the ceiling row (world y 6), where the light panels replace ceiling tiles. Rooms own a wall on their
west and north edge only, so two neighbouring rooms never double up their walls. Doorways are
connected through jigsaw blocks:

    west jigsaw   name door_w  target out_e
    north jigsaw  name door_n  target out_s
    east jigsaw   name out_e   target door_w
    south jigsaw  name out_s   target door_n
"""
import itertools
import json
import os
import random

from nbtlib import Compound, File, Int, List, String

MOD = "backrooms26_3"
DATA_VERSION = 5023
ROOT = os.path.join("src", "main", "resources", "data", MOD)
STRUCTURE_DIR = os.path.join(ROOT, "structure", "level_0")
POOL_DIR = os.path.join(ROOT, "worldgen", "template_pool", "level_0")

SIZE = (16, 5, 16)
WALL_HEIGHT = 4
DOOR_HEIGHT = 3
DOOR_RANGE = range(6, 10)
JIGSAW_POS = 7

WALLPAPER = 0
LIGHT = 1
JIGSAW_STATE = {"east": 2, "west": 3, "north": 4, "south": 5}

PALETTE = [
    Compound({"id": String(f"{MOD}:yellow_wallpaper")}),
    Compound({"id": String(f"{MOD}:fluorescent_light")}),
] + [
    Compound({"id": String("minecraft:jigsaw"), "properties": Compound({"orientation": String(f"{d}_up")})})
    for d in ("east", "west", "north", "south")
]

LIGHT_PANELS = [
    [(x, z) for x in (3, 4) for z in range(2, 6)],
    [(x, z) for x in (11, 12) for z in range(2, 6)],
    [(x, z) for x in (3, 4) for z in range(10, 14)],
    [(x, z) for x in (11, 12) for z in range(10, 14)],
]

INTERIORS = {"open": 4, "pillars": 2, "partition": 2, "alcove": 1}
DOORS = {"wn": 3, "w": 2, "n": 2}
OUTPUTS = {"es": 4, "e": 2, "s": 2, "x": 1}


def interior_cells(kind):
    cells = set()
    if kind == "pillars":
        for px in (4, 10):
            for pz in (4, 10):
                for dx in range(2):
                    for dz in range(2):
                        cells.add((px + dx, pz + dz))
    elif kind == "partition":
        for z in range(1, 15):
            if z not in DOOR_RANGE:
                cells.add((11, z))
    elif kind == "alcove":
        for i in range(10, 15):
            cells.add((10, i))
            cells.add((i, 10))
    return cells


def build_room(doors, outputs, interior, rng):
    blocks = {}

    def wall(x, z, gap=False):
        for y in range(WALL_HEIGHT):
            if gap and y < DOOR_HEIGHT:
                continue
            blocks[(x, y, z)] = (WALLPAPER, None)

    for i in range(16):
        wall(0, i, gap="w" in doors and i in DOOR_RANGE)
        wall(i, 0, gap="n" in doors and i in DOOR_RANGE)

    for (x, z) in interior_cells(interior):
        wall(x, z)

    for panel in rng.sample(LIGHT_PANELS, rng.choice([4, 3, 3, 2, 2, 1, 0])):
        for (x, z) in panel:
            blocks[(x, WALL_HEIGHT, z)] = (LIGHT, None)

    def jigsaw(pos, direction, name, target):
        blocks[pos] = (JIGSAW_STATE[direction], Compound({
            "id": String("minecraft:jigsaw"),
            "name": String(f"{MOD}:{name}"),
            "target": String(f"{MOD}:{target}"),
            "pool": String(f"{MOD}:level_0/rooms"),
            "final_state": String("minecraft:air"),
            "joint": String("rollable"),
        }))

    if "w" in doors:
        jigsaw((0, 0, JIGSAW_POS), "west", "door_w", "out_e")
    if "n" in doors:
        jigsaw((JIGSAW_POS, 0, 0), "north", "door_n", "out_s")
    if "e" in outputs:
        jigsaw((15, 0, JIGSAW_POS), "east", "out_e", "door_w")
    if "s" in outputs:
        jigsaw((JIGSAW_POS, 0, 15), "south", "out_s", "door_n")
    return blocks


def write_template(name, blocks):
    entries = []
    for (x, y, z), (state, nbt) in sorted(blocks.items()):
        entry = Compound({"pos": List[Int]([Int(x), Int(y), Int(z)]), "state": Int(state)})
        if nbt is not None:
            entry["nbt"] = nbt
        entries.append(entry)
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


def main():
    os.makedirs(STRUCTURE_DIR, exist_ok=True)
    os.makedirs(POOL_DIR, exist_ok=True)
    rng = random.Random(2603)
    rooms = []
    start = []
    for doors, outputs, interior in itertools.product(DOORS, OUTPUTS, INTERIORS):
        name = f"room_{doors}_{outputs}_{interior}"
        write_template(name, build_room(doors, outputs, interior, rng))
        weight = DOORS[doors] * OUTPUTS[outputs] * INTERIORS[interior]
        rooms.append(pool_element(name, weight))
        if doors == "wn" and outputs == "es":
            start.append(pool_element(name, 1))
    write_pool("rooms", rooms)
    write_pool("start", start)
    print(f"wrote {len(rooms)} rooms")


if __name__ == "__main__":
    main()
