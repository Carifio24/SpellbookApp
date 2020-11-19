import os
import json

assets_dir = os.path.join("..", "app", "src", "main", "assets")
lists_file = "TGE_expanded_lists.json"
spells_file = "Spells.json"

TGE_KEY = "tge_expanded_classes"

lists_filepath = os.path.join(assets_dir, lists_file)
spells_filepath = os.path.join(assets_dir, spells_file)
with open(lists_filepath, 'r') as f:
    tge_lists = json.load(f)
with open(spells_filepath, 'r') as f:
    spells = json.load(f)


for data in tge_lists:
    classname = data["class"]
    class_spells = set(data["spells"])
    for spell in spells:
        name = spell["name"]
        if name in class_spells:
            if TGE_KEY in spell.keys():
                if classname not in spell[TGE_KEY]:
                    spell[TGE_KEY] += [ classname ]
                class_spells.remove(name)
            else:
                spell[TGE_KEY] = [ classname ]
            
    if len(class_spells) != 0:
        print("\n=====\nMissing spells for %s:" % classname)
        for spell in class_spells:
            print(spell)

with open(spells_filepath, 'w') as f:
    json.dump(spells, f, indent=4, sort_keys=True)