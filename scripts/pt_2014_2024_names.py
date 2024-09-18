from json import dump, load
from os.path import join

filepath = join("..", "app", "src", "main", "assets", "Spells_pt.json")
map_file = "2014_to_2024_ids.txt"

id_map = {}
new_to_old = {}
with open(map_file, 'r') as f:
    for line in f:
        old, new = [int(x) for x in line.split(",")]
        new_to_old[new] = old

with open(filepath, 'r') as f:
    spells = load(f)

for spell in spells:
    id_map[spell["id"]] = spell

for spell in spells:
    old_id = new_to_old.get(spell["id"], None)
    if old_id is None:
        continue
    
    old_spell = id_map[old_id]

    spell["name"] = old_spell["name"]

with open(filepath, 'w', encoding="utf8") as f:
    dump(spells, f, indent=4, sort_keys=True, ensure_ascii=False)
