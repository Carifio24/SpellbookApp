import os
import sys
import json

def get_spell_id(name):

    spells_file = os.path.join("..", "app", "src", "main", "assets", "Spells.json")
    with open(spells_file, 'r') as f:
        spells = json.load(f)

    for spell in spells:
        if spell["name"].lower() == name.lower():
            return spell["id"]
    return -1


while True:
    name = input("Name: ")
    print(get_spell_id(name))