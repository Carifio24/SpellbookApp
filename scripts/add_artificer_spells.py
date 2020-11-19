import os
import json

artificer_spells = set([
    "Acid Splash",
    "Booming Blade",
    "Create Bonfire",
    "Dancing Lights",
    "Fire Bolt",
    "Frostbite",
    "Green-Flame Blade",
    "Guidance",
    "Light",
    "Lightning Lure",
    "Mage Hand",
    "Magic Stone",
    "Mending",
    "Message",
    "Poison Spray",
    "Prestidigitation",
    "Ray of Frost",
    "Resistance",
    "Shocking Grasp",
    "Spare the Dying",
    "Sword Burst",
    "Thorn Whip",
    "Thunderclap",
    "Absorb Elements",
    "Alarm",
    "Catapult",
    "Cure Wounds",
    "Detect Magic",
    "Disguise Self",
    "Expeditious Retreat",
    "Faerie Fire",
    "False Life",
    "Feather Fall",
    "Grease",
    "Identify",
    "Jump",
    "Longstrider",
    "Purify Food and Drink",
    "Sanctuary",
    "Snare",
    "Tasha's Caustic Brew",
    "Aid",
    "Alter Self",
    "Arcane Lock",
    "Blur",
    "Continual Flame",
    "Darkvision",
    "Enhance Ability",
    "Enlarge/Reduce",
    "Heat Metal",
    "Invisibility",
    "Lesser Restoration",
    "Levitate",
    "Magic Weapon",
    "Protection from Poison",
    "Pyrotechnics",
    "Rope Trick",
    "See Invisibility",
    "Skywrite",
    "Spider Climb",
    "Web",
    "Blink",
    "Catnap",
    "Create Food and Water",
    "Dispel Magic",
    "Elemental Weapon",
    "Flame Arrows",
    "Fly",
    "Glyph of Warding",
    "Haste",
    "Intellect Fortress",
    "Protection from Energy",
    "Revivify",
    "Tiny Servant",
    "Water Breathing",
    "Water Walk",
    "Arcane Eye",
    "Elemental Bane",
    "Fabricate",
    "Freedom of Movement",
    "Leomund's Secret Chest",
    "Mordenkainen's Faithful Hound",
    "Mordenkainen's Private Sanctum",
    "Otiluke's Resilient Sphere",
    "Stone Shape",
    "Stoneskin",
    "Summon Construct",
    "Animate Objects",
    "Bigby's Hand",
    "Creation",
    "Greater Restoration",
    "Skill Empowerment",
    "Transmute Rock",
    "Wall of Stone"
])
ARTIFICER = "Artificer"

spells_dir = os.path.join("..", "app", "src", "main", "assets")
spells_file = "Spells.json"

spells_filepath = os.path.join(spells_dir, spells_file)
with open(spells_filepath, 'r') as f:
    spells = json.load(f)

start_id = 461
id = start_id
for spell in spells:
    name = spell["name"]
    if name in artificer_spells:
        artificer_spells.remove(name)
        if ARTIFICER not in spell["classes"]:
            spell["classes"] = [ ARTIFICER ] + spell["classes"]

if len(artificer_spells) != 0:
    print("Missing spells:")
    for spell in artificer_spells:
        print(spell)

with open(spells_filepath, 'w') as f:
    json.dump(spells, f, indent=4, sort_keys=True)