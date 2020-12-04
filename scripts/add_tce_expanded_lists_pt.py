import os
import json

def load_spells(filename):
    with open(filename, 'r') as f:
        spells = json.load(f)
    return spells

def id_map(spells):
    return { spell["id"] : spell for spell in spells }

DATA_DIR = "../app/src/main/assets"
TCE_CLASSES_KEY = "tce_expanded_classes"
EN_TO_PT_CLASSES = {
    "Artificer" : "Artífice",
    "Bard" : "Bardo",
    "Cleric" : "Clérigo",
    "Druid" : "Druida",
    "Paladin" : "Paladino",
    "Ranger" : "Patrulheiro",
    "Sorcerer" : "Feiticeiro",
    "Warlock" : "Bruxo",
    "Wizard" : "Mago"
}

en_file = os.path.join(DATA_DIR, "Spells.json")
pt_file = os.path.join(DATA_DIR, "Spells_pt_backup.json")

en_spells = load_spells(en_file)
pt_spells = load_spells(pt_file)
en_map = id_map(en_spells)
pt_map = id_map(pt_spells)

for en_spell in en_spells:
    if TCE_CLASSES_KEY not in en_spell:
        continue
    tce_classes = en_spell[TCE_CLASSES_KEY]
    tce_classes = list(sorted([ EN_TO_PT_CLASSES[cc] for cc in tce_classes ]))
    pt_spell = pt_map[en_spell["id"]]
    pt_spell[TCE_CLASSES_KEY] = tce_classes

out_pt_file = os.path.join(DATA_DIR, "Spells_pt.json")
pt_spells = list(sorted(pt_spells, key = lambda x: x["name"]))
with open(out_pt_file, 'w', encoding="utf-8") as f:
    json.dump(pt_spells, f, indent=4, sort_keys=True, ensure_ascii=False)