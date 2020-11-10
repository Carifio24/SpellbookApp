import os
import json

spells_file = os.path.join("..", "app", "src", "main", "assets", "Spells.json")
with open(spells_file, 'r') as f:
    spells = json.load(f)

id = 1
for spell in spells:
    spell["id"] = id
    id += 1

with open(spells_file, 'w') as f:
    json.dump(spells, f, indent=4, sort_keys=True)