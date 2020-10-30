import sys
import string
import codecs

# Useful strings
casting_time_pt = "Tempo de Conjuração"
range_pt = "Alcance"
components_pt = "Componentes"
duration_pt = "Duração"
higher_level_pt = "Em Níveis Superiores"
concentration_pt = "Concentração"
location_pt = "Localização"

# Keys
casting_time_k = "casting_time"
range_k = "range"
components_k = "components"
duration_k = "duration"
higher_level_k = "higher_level"

items = [ "school_level", casting_time_k, range_k, components_k, duration_k, "location" ]
items_pt = [ "school_level", casting_time_pt, range_pt, components_pt, duration_pt, location_pt ]

# Mapping from Portuguese phrases to keys
keymap = {
    casting_time_pt: "casting_time",
    range_pt : "range",
    components_pt: "components",
    duration_pt: "duration",
    higher_level_pt: "higher_level"
}

# For parsing school and level
digits = [ (w, str(w)) for w in range(1, 10) ]
def parse_level_school(line):
    print(line)
    if line.endswith("(ritual)"):
        ritual = True
        school_idx = -2
    else:
        ritual = False
        school_idx = -1
    school = string.capwords(line.split()[school_idx])
    
    for d, s in digits:
        if line.startswith(s):
            return d, school, ritual
    else:
        return 0, school, ritual


# For parsing location (source and page)
def parse_location(s):
    source, page = s.split()
    return source, int(page)


sourcebook_code = "CEGA"
filename = "%sSpellText.txt" % sourcebook_code
spells = []
spell = { "desc": [], "higher_level": []}
idx = 0
lines = []
with codecs.open(filename, 'r', encoding='utf-8') as f:
    for x in f:

        # Omit the trailing newline
        line = x.strip()

        # If the line is all caps, it's the name of a new spell
        # So we need to add this spell to the list and reset
        if line.isupper():
            if len(spell) > 2:
                spell["desc"] = "\n".join([ x for x in spell["desc"] if len(x) > 0])
                spell["higher_level"] = "\n".join([ x for x in spell["higher_level"] if len(x) > 0])
                spells.append(spell)
                spell = { "desc": [], "higher_level": [] }
            spell["name"] = string.capwords(line)
            print(spell["name"])
            idx = 0
            continue

        # Otherwise, do the appropriate thing

        # School and level
        if idx == 0:
            level, school, ritual = parse_level_school(line)
            spell["level"] = level
            spell["school"] = school
            spell["ritual"] = ritual
            idx += 1
            continue

        # Casting time, range, components, duration
        should_continue = False
        while idx in range(1 ,5):
            item = items[idx]
            item_pt = items_pt[idx]
            if line.startswith(items_pt[idx+1]):
                spell[item] = " ".join([s for s in lines if len(s) > 0])
                lines = []
                idx += 1
            else:
                if line.startswith(item_pt):
                    line = line[len(item_pt)+2:]
                lines.append(line)
                should_continue = True
                break
        if should_continue:
            continue

        # Location
        if idx == 5:
            line = line[len(items_pt[idx])+2:]
            print(line)
            source, page = parse_location(line)
            spell["sourcebook"] = source
            spell["page"] = int(page)
            idx += 1
            continue

        # Description
        if idx == 6:
            if line.startswith(higher_level_pt):
                lines = []
                idx += 1
            else:
                if len(line) == 0:
                    spell["desc"].append(" ".join(lines))
                    lines = []
                else:
                    lines.append(line)
                continue


        # Higher level
        if idx == 7:
            if len(line) == 0:
                spell["higher_level"].append(" ".join(lines))
                lines = []
            else:
                lines.append(line)




# Add the last spell when we hit EOF
spell["desc"] = "\n".join(spell["desc"])
spell["higher_level"] = "\n".join(spell["higher_level"])
spells.append(spell)


# Clean up the components and duration for each spell
for spell in spells:

    print(spell)

    # For testing only
    #spell["classes"] = [ "Mago" ]
 
    # Components cleanup
    components_text = spell["components"]
    components = components_text.split(None, 3)
    if 'M' in components:
        idx = components_text.index('(')
        components = components_text[:idx]
        materials = components_text[idx+1:-1]
        materials = materials[:1].upper() + materials[1:]
        spell["components"] = components.split(None, 3)
        spell["materials"] = materials
    else:
        spell["components"] = components


    # Duration cleanup
    duration_text = spell["duration"]
    if duration_text.startswith(concentration_pt):
        duration = duration_text[len(concentration_pt)+2:].capitalize() # Account for the ", "
        spell["duration"] = duration
        concentration = True
    else:
        concentration = False
    spell["concentration"] = concentration


# When we're done, we want to write the spells to a file
import json
output_filename = "../../app/src/main/assets/%s_spells_pt.json" % sourcebook_code
with codecs.open(output_filename, 'w', encoding='utf-8') as f:
    f.write(json.dumps(spells, ensure_ascii=False, indent=4, sort_keys=True))