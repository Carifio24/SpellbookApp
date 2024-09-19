# This is only going to run once
# No need to think about efficiency

import json
from os.path import join


folder = join("..", "app", "src", "main", "assets")
path_2014 = join(folder, "Spells_en.json")
path_2024 = "2024_spells.json"

with open(path_2014, 'r') as f:
    spells_2014 = json.load(f)

with open(path_2024, 'r') as f:
    spells_2024 = json.load(f)

linked = set()
by_name_2014 = {spell["name"]: spell["id"] for spell in spells_2014 if spell["id"] <= 530}
for spell in spells_2024:
    name = spell["name"]
    if name in by_name_2014:
        linked.add((by_name_2014[name], spell["id"]))

outfile = "2014_to_2024_ids.txt"
mapfile = "2014_to_2024_id_map.java"
with open(outfile, 'w') as f, \
     open(mapfile, 'w') as g:
    for pair in linked:
        f.write(f"{pair[0]},{pair[1]}\n")
        g.write(f"put({pair[0]}, {pair[1]});\n")
