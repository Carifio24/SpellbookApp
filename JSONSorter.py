import os
import json

file_dir = "app/src/main/assets"

spell_file = os.path.join(file_dir, "Spells.json")

with open(spell_file, 'r') as f:
    json_list = json.loads(f.read())

json_sorted = list(sorted(json_list, key=lambda js : js["name"]))
sorted_file = os.path.join(file_dir, "SpellsByName.json")
with open(sorted_file, 'w') as f:
    f.write(json.dumps(json_sorted, indent=4, sort_keys=True))