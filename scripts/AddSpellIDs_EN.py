import os
import json

spells_dir = os.path.join("..", "app", "src", "main", "assets")
spells_file = "Spells_TGE_en.json"
spells_filepath = os.path.join(spells_dir, spells_file)
with open(spells_filepath, 'r') as f:
    spells = json.load(f)

start_id = 461
id = start_id
for spell in spells:
    spell["id"] = id
    id += 1

with open(spells_filepath, 'w') as f:
    json.dump(spells, f, indent=4, sort_keys=True)