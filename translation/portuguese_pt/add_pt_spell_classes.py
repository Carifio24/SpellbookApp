import sys
import json
import codecs

# The spell lists
from pt_spell_lists import *
class_lists = [ # They're actually sets
    ("Bardo", bard_spells),
    ("Clérigo", cleric_spells),
    ("Druida", druid_spells),
    ("Paladino", paladin_spells),
    ("Patrulheiro", ranger_spells),
    ("Feiticeiro", sorcerer_spells),
    ("Bruxo", warlock_spells),
    ("Mago", wizard_spells)
]

filename = "../../app/src/main/assets/Spells_pt.json"
with codecs.open(filename, 'r', encoding='utf-8') as f:
    spells = json.load(f)

for spell in spells:
    classes = []
    for caster, lst in class_lists:
        if spell["name"] in lst:
            classes.append(caster)

    if len(classes) == 0:
        print("The spell %s has no classes!" % spell["name"])
        sys.exit(0)

    spell["classes"] = classes


with codecs.open(filename, 'w', encoding='utf-8') as f:
    f.write(json.dumps(spells, indent=4, ensure_ascii=False, sort_keys=True))