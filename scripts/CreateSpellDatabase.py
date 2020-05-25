import os
import json
import sqlite3

def bool_to_int(b):
    return 1 if b else 0


def main():

    # The location of the assets directory
    assets_dir = os.path.join("..", "app", "src", "main", "assets")

    # The location of the spells JSON, and our output location
    json_file = os.path.join(assets_dir, "Spells.json")
    db_file = os.path.join(assets_dir, "spell_database.db")

    # Create the connection and get a cursor
    conn = sqlite3.connect(db_file)
    c = conn.cursor()

    # Create the table
    create_command = """CREATE TABLE spells (name text, description text, higher_level text,
                                            page integer, verbal integer, somatic integer,
                                            material integer, materials text, ritual integer, 
                                            concentration integer, range text, duration text,
                                            casting_time text, level integer, school text,
                                            sourcebook text, classes text, subclasses text,
                                            created integer
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
        tpl = (spell["name"], spell["desc"], spell["higher_level"], spell["page"], verbal, somatic, material, spell["material"], ritual, concentration, spell["range"], spell["duration"], spell["casting_time"], spell["level"], spell["school"], spell["sourcebook"], classes_str, subclasses_str, created)
        spell_tuples.append(tpl)

    c.executemany('INSERT INTO spells VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)', spell_tuples)

    conn.commit()
    conn.close()
    


if __name__ == "__main__":
    main()