import os
import json
import sqlite3


unit_values = {
    "foot"  : 1,
    "feet": 1,
    "mile" : 5280,
    "miles": 5280,
    "seconds" : 1,
    "minutes": 60,
    "minute": 60,
    "hour": 60*60,
    "hours": 60*60,
    "day": 24*60*60,
    "days": 24*60*60,
    "year": 365*24*60*60,
    "years": 365*24*60*60,
    "round": 6,
    "rounds": 6
}


def bool_to_int(b):
    return 1 if b else 0


def parse_duration(text):

    # The nonspanning types
    nonspanning_types = [ "Special", "Instantaneous", "Until dispelled" ]
    for t in nonspanning_types:
        if text.startswith(t):
            return [ t, 0, "second", 0, text ]


    # Cut off the concentration prefix, it it's there
    concentration_prefix = "Up to"
    original_text = text
    if text.startswith(concentration_prefix):
        text = text[len(concentration_prefix):]

    # If we have a finite duration
    pieces = text.split(maxsplit=1)
    value = int(pieces[0])
    unit = pieces[1]
    base_value = value * unit_values[unit]
    return [ "Finite duration", value, unit, base_value, original_text ]


def parse_range(text):

    # The unusual range types
    unusual_types = [ "Touch", "Special", "Sight", "Unlimited" ]
    for t in unusual_types:
        if text.startswith(t):
            return [ t, 0, "foot", 0, text ]


    # Self and ranged types
    # First, self
    pieces = text.split(" ", 1)
    if text.startswith("Self"):
        
        if len(pieces) == 1:
            return [ "Self", 0, "foot", 0, text ]
        else:
            dist = pieces[1]
            if not (dist.startswith("(") and dist.endswith(")")):
                raise ValueError("Error parsing radius of Self spell: %s" % text)
            dist = dist[1:-1]
            dist_pcs = dist.split(" ")
            length = int(dist_pcs[0])
            unit = dist_pcs[1]
            base_value = length * unit_values[unit]
            return [ "Self", length, unit, base_value, text ]
    # Then ranged
    else:
        length = int(pieces[0])
        unit = pieces[1]
        base_value = length * unit_values[unit]
        return [ "Finite range", length, unit, base_value, text ]


def parse_casting_time(text):

    pieces = text.split(" ", maxsplit=1)
    value = int(pieces[0])
    ctt = pieces[1]

    # If we have an action type
    action_types = [ "action", "bonus action", "reaction" ]
    for t in action_types:
        if ctt.startswith(t):
            return [ t, value * 6, "second", value * 6, text ]

    # Otherwise, ctt is just the unit
    base_value = value * unit_values[ctt]
    return [ "time", value, ctt, base_value, text ]



def main():

    # The location of the assets directory
    assets_dir = os.path.join("..", "app", "src", "main", "assets")

    # The location of the spells JSON, and our output location
    json_file = os.path.join(assets_dir, "Spells.json")
    db_file = os.path.join(assets_dir, "spell_database.db")

    # Delete the database file if it already exists
    if os.path.isfile(db_file):
        os.remove(db_file)

    # Create the connection and get a cursor
    conn = sqlite3.connect(db_file)
    c = conn.cursor()

    # Create the table
    create_command = """CREATE TABLE spells (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, description TEXT, higher_level TEXT,
                                            page INTEGER NOT NULL, verbal INTEGER NOT NULL, somatic INTEGER NOT NULL,
                                            material INTEGER NOT NULL, materials TEXT, ritual INTEGER NOT NULL, concentration INTEGER NOT NULL,
                                            range_type TEXT, range_value INTEGER, range_unit_type TEXT, range_base_value INTEGER, range_description TEXT,
                                            duration_type TEXT, duration_value INTEGER, duration_unit_type TEXT, duration_base_value INTEGER, duration_description TEXT,
                                            casting_time_type TEXT, casting_time_value INTEGER, casting_time_unit_type TEXT, casting_time_base_value INTEGER, casting_time_description TEXT,
                                            level INTEGER NOT NULL, school TEXT, sourcebook TEXT, classes TEXT, subclasses TEXT,
                                            created INTEGER NOT NULL
                                            )"""
    c.execute(create_command)

    
    with open(json_file, 'r') as f:
        spells_json = json.load(f)
    


    spell_tuples = []
    for spell in spells_json:
        ritual = bool_to_int(spell["ritual"])
        concentration = bool_to_int(spell["concentration"])
        components = spell["components"]
        verbal = "V" in components
        somatic = "S" in components
        material = "M" in components
        classes_str = ",".join(spell["classes"])
        subclasses_str = ",".join(spell["subclasses"])
        created = 0
        higher_level_text = spell["higher_level"]
        higher_level = higher_level_text if higher_level_text else None
        materials_text = spell["material"]
        materials = materials_text if materials_text else None
        tpl = (spell["name"], spell["desc"], higher_level, spell["page"], verbal, somatic, material, materials, ritual, concentration, *parse_range(spell["range"]), *parse_duration(spell["duration"]), *parse_casting_time(spell["casting_time"]), spell["level"], spell["school"], spell["sourcebook"], classes_str, subclasses_str, created)
        spell_tuples.append(tpl)

    c.executemany("""
    INSERT INTO spells (name, description, higher_level, page, verbal, somatic, material, materials, ritual, concentration, range_type, range_value, range_unit_type, range_base_value, range_description,
     duration_type, duration_value, duration_unit_type, duration_base_value, duration_description, casting_time_type, casting_time_value, casting_time_unit_type, casting_time_base_value, casting_time_description,
     level, school, sourcebook, classes, subclasses, created)
    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""", spell_tuples)

    conn.commit()
    conn.close()
    


if __name__ == "__main__":
    main()