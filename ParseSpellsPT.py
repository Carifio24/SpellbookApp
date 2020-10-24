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

# Mapping from Portuguese phrases to keys
keymap = {
    casting_time_pt: "casting_time",
    range_pt : "range",
    components_pt: "components",
    duration_pt: "duration",
    higher_level_pt: "higher_level"
}

flags = {
    casting_time_pt: False,
    components_pt: False,
    duration_pt: False,
    range_pt: False
}

digits = [ (w, str(w)) for w in range(1, 10) ]
def parse_level_school(line):
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

filename = "LDJSpellText.txt"
spells = []
spell = None
text = ""
curr_flag_key = None
need_level_school = False
with codecs.open(filename, 'r', encoding='utf-8') as f:
    for x in f:

        #print("x is %s" % x)

        # Omit the trailing newline character
        line = x.strip()

        # Skip a blank line
        # If one of the flags was set, unset it and update the spell
        if len(line) == 0:
            if curr_flag_key is not None:
                #print("Assigning %s to %s" % (text, keymap[curr_flag_key]))
                spell[keymap[curr_flag_key]] = text
                text = ""
                curr_flag_key = None
                flags = { k : False for k in flags.keys() }
            continue

        # If we need the level/school line
        if need_level_school:
            need_level_school = False
            level, school, ritual = parse_level_school(line)
            spell["level"] = level
            spell["school"] = school
            spell["ritual"] = ritual
            continue

        # If the line is all caps, it's the name of a new spell
        # So we need to add the old one to the list, and start a new one
        if line.isupper():

            # Add the old one, unless it's empty
            # (i.e. the first time)
            if spell:
                if curr_flag_key is not None:
                    spell[keymap[curr_flag_key]] = text
                else:
                    spell["desc"] = text.rstrip()
                spells.append(spell)

            # Now add this new line, un-capitalized, as the name
            spell = {}
            spell["name"] = string.capwords(line)

            need_level_school = True

            # Reset all flags to False
            flags = { k : False for k in flags.keys() }
            continue


        # If it's not a new name, we need to check what info it's giving
        # Check if it's giving the casting time, range, duration, or components
        found_key = False
        for k in keymap.keys():
            if line.startswith(k):

                # Add the old text to the spell
                if len(text) != 0:
                    if curr_flag_key is not None:
                        #print("Assigning %s to %s" % (text, keymap[curr_flag_key]))
                        spell[keymap[curr_flag_key]] = text
                    else:
                        #print("Assigning %s to desc" % text)
                        spell["desc"] = text.rstrip()

                # Update the flag and start the new field
                curr_flag_key = k
                text = line[len(k)+2:]
                #print("line is %s" % line)
                #print("text = %s" % text)
                found_key = True
                break
        if found_key:
            continue

        # Otherwise, just continue with the text block that we're on
        text += " " + line
        #print("line is %s" % line)
        #print("text = %s" % text)


# Clean up the components and duration for each spell
# also, add the sourcebooks
for spell in spells:
    print(spell)

    spell["sourcebook"] = "PHB"
    if "higher_level" not in spell.keys():
        spell["higher_level"] = ""

    # For testing only
    spell["page"] = 0
    spell["classes"] = [ "Mago" ]
 
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
output_filename = "app/src/main/assets/Spells_pt.json"
with codecs.open(output_filename, 'w', encoding='utf-8') as f:
    f.write(json.dumps(spells, ensure_ascii=False, indent=4, sort_keys=True))