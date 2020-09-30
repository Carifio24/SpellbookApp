import os
import json
import sqlite3
from itertools import product

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

sources = {
    "PHB" : (1, "Player's Handbook"),   
    "XGE" : (2, "Xanathar's Guide to Everything"),
    "SCAG" : (3, "Sword Coast Adv. Guide"),
    "LLK" : (4, "Lost Laboratory of Kwalish"),
    "AI" : (5, "Acquisitions Incorporated"),
}

schools = {
    "Abjuration" : 1,
    "Conjuration" : 2,
    "Divination" : 3,
    "Enchantment" : 4,
    "Evocation" : 5,
    "Illusion" : 6,
    "Necromancy" : 7,
    "Transmutation" : 8
}

classes = {
    "Bard" : 1,
    "Cleric" : 2,
    "Druid" : 3,
    "Paladin" : 4,
    "Ranger" : 5,
    "Sorcerer" : 6,
    "Warlock" : 7,
    "Wizard" : 8
}

def value_sorted_keys(d):
    return [ e[0] for e in sorted(d.items(), key=lambda x: x[1]) ]

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



def create_spells_table(spellbook_json, connection):
    c = connection.cursor()

    # Create the table
    create_command = """CREATE TABLE spells (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, description TEXT, higher_level TEXT,
                                            page INTEGER NOT NULL, verbal INTEGER NOT NULL, somatic INTEGER NOT NULL,
                                            material INTEGER NOT NULL, materials TEXT, ritual INTEGER NOT NULL, concentration INTEGER NOT NULL,
                                            range_type TEXT, range_value INTEGER, range_unit_type TEXT, range_base_value INTEGER, range_description TEXT,
                                            duration_type TEXT, duration_value INTEGER, duration_unit_type TEXT, duration_base_value INTEGER, duration_description TEXT,
                                            casting_time_type TEXT, casting_time_value INTEGER, casting_time_unit_type TEXT, casting_time_base_value INTEGER, casting_time_description TEXT,
                                            level INTEGER NOT NULL, school_id INTEGER NOT NULL, source_id INTEGER NOT NULL,
                                            created INTEGER NOT NULL,
                                            FOREIGN KEY("school_id") REFERENCES "schools"("id") ON DELETE CASCADE ON UPDATE CASCADE,
                                            FOREIGN KEY("source_id") REFERENCES "sources"("id") ON DELETE CASCADE ON UPDATE CASCADE
                                            )"""
    c.execute(create_command)

    
    spell_tuples = []
    created = 0
    for spell in spellbook_json:
        ritual = bool_to_int(spell["ritual"])
        concentration = bool_to_int(spell["concentration"])
        components = spell["components"]
        verbal = "V" in components
        somatic = "S" in components
        material = "M" in components
        higher_level_text = spell["higher_level"]
        higher_level = higher_level_text if higher_level_text else None
        materials_text = spell["material"]
        materials = materials_text if materials_text else None
        tpl = (spell["name"], spell["desc"], higher_level, spell["page"], verbal, somatic, material, materials, ritual, concentration, *parse_range(spell["range"]), *parse_duration(spell["duration"]), *parse_casting_time(spell["casting_time"]), spell["level"], spell["school"], sources[spell["sourcebook"]][0], created)
        spell_tuples.append(tpl)

    c.executemany("""
    INSERT INTO spells (name, description, higher_level, page, verbal, somatic, material, materials, ritual, concentration, range_type, range_value, range_unit_type, range_base_value, range_description,
     duration_type, duration_value, duration_unit_type, duration_base_value, duration_description, casting_time_type, casting_time_value, casting_time_unit_type, casting_time_base_value, casting_time_description,
     level, school_id, source_id, created)
    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""", spell_tuples)

    connection.commit()
    

def create_db():

    # The location of the assets directory
    assets_dir = os.path.join("..", "app", "src", "main", "assets")

    # The location of the spells JSON, and our output location
    db_file = os.path.join(assets_dir, "spellbook.db")

    # Delete the database file if it already exists
    if os.path.isfile(db_file):
        os.remove(db_file)

    # Create the connection and get a cursor
    conn = sqlite3.connect(db_file)
    return conn


def parse_json():

    # The location of the assets directory
    assets_dir = os.path.join("..", "app", "src", "main", "assets")

    # Parse the JSON
    json_file = os.path.join(assets_dir, "Spells.json")
    with open(json_file, 'r') as f:
        sb_json = json.load(f)
    return sb_json


def create_sql_table(connection, create_command, populate_command=None, data=None):

    # Create the table
    c = connection.cursor()
    c.execute(create_command)

     # Populate the table, if necessary
    if populate_command is not None and data is not None:
        c.executemany(populate_command, data)

    # Commit
    connection.commit()


def create_sources_table(connection):

    # Create the table
    create_command = """
                CREATE TABLE sources (
                "id"	INTEGER NOT NULL,
                "name"	TEXT NOT NULL,
                "code"	TEXT,
                "created"	INTEGER NOT NULL,
                PRIMARY KEY("id" AUTOINCREMENT)
                );
                """
    source_data = [ (v[1], k, 0) for k, v in sources.items() ]
    populate_command = "INSERT INTO sources (name, code, created) VALUES (?, ?, ?)"
    
    create_sql_table(connection, create_command, populate_command, source_data)


def create_characters_table(connection):

    create_command = """
                CREATE TABLE characters (
                "id"	INTEGER NOT NULL,
                "name"	TEXT NOT NULL,
                "first_sort_field"	TEXT,
                "second_sort_field"	TEXT,
                "first_sort_reverse"	INTEGER NOT NULL DEFAULT 0,
                "second_sort_reverse"	INTEGER NOT NULL DEFAULT 0,
                "status_filter"	TEXT,
                "min_level"	INTEGER NOT NULL DEFAULT 0,
                "max_level"	INTEGER NOT NULL DEFAULT 9,
                "ritual_filter"	INTEGER NOT NULL DEFAULT 1,
                "not_ritual_filter"	INTEGER NOT NULL DEFAULT 1,
                "concentration_filter"	INTEGER NOT NULL DEFAULT 1,
                "not_concentration_filter"	INTEGER NOT NULL DEFAULT 1,
                "verbal_filter"	INTEGER NOT NULL DEFAULT 1,
                "not_verbal_filter"	INTEGER NOT NULL DEFAULT 1,
                "somatic_filter"	INTEGER NOT NULL DEFAULT 1,
                "not_somatic_filter"	INTEGER NOT NULL DEFAULT 1,
                "material_filter"	INTEGER NOT NULL DEFAULT 1,
                "not_material_filter"	INTEGER NOT NULL DEFAULT 1,
                "visible_casting_time_types"	TEXT,
                "visible_duration_types"	TEXT,
                "visible_range_types"	TEXT,
                "min_duration_type"	TEXT,
                "min_duration_value"	INTEGER,
                "min_duration_unit_type"	TEXT,
                "min_duration_base_value"	INTEGER,
                "min_duration_description"	TEXT,
                "max_duration_type"	TEXT,
                "max_duration_value"	INTEGER,
                "max_duration_unit_type"	TEXT,
                "max_duration_base_value"	INTEGER,
                "max_duration_description"	TEXT,
                "min_casting_time_type"	TEXT,
                "min_casting_time_value"	INTEGER,
                "min_casting_time_unit_type"	TEXT,
                "min_casting_time_base_value"	INTEGER,
                "min_casting_time_description"	TEXT,
                "max_casting_time_type"	TEXT,
                "max_casting_time_value"	INTEGER,
                "max_casting_time_unit_type"	TEXT,
                "max_casting_time_base_value"	INTEGER,
                "max_casting_time_description"	TEXT,
                "min_range_type"	TEXT,
                "min_range_value"	INTEGER,
                "min_range_unit_type"	TEXT,
                "min_range_base_value"	INTEGER,
                "min_range_description"	TEXT,
                "max_range_type"	TEXT,
                "max_range_value"	INTEGER,
                "max_range_unit_type"	TEXT,
                "max_range_base_value"	INTEGER,
                "max_range_description"	TEXT,
                PRIMARY KEY("id" AUTOINCREMENT)
            );
            """
    create_sql_table(connection, create_command)


def create_classes_table(connection):

    create_command = """
                CREATE TABLE classes (
                "id"	INTEGER NOT NULL,
                "name"	TEXT NOT NULL,
                PRIMARY KEY("id" AUTOINCREMENT)
            );
            """
    

    populate_command = "INSERT INTO classes (name) VALUES (?)"
    class_names = [ [x] for x in value_sorted_keys(classes) ]
    create_sql_table(connection, create_command, populate_command, class_names)


def create_schools_table(connection):

    create_command = """
                CREATE TABLE schools (
                "id"	INTEGER NOT NULL,
                "name"	TEXT NOT NULL,
                PRIMARY KEY("id" AUTOINCREMENT)
            );
            """

    populate_command = "INSERT INTO schools (name) VALUES (?)"
    school_names = [ [x] for x in value_sorted_keys(schools) ]
    create_sql_table(connection, create_command, populate_command, school_names)


def create_simple_join_table(connection, join_table_name, table1_name, table2_name, id1_name, id2_name, data=None):

    create_command = """
                    CREATE TABLE %s (
                    "%s" INTEGER NOT NULL,
                    "%s" INTEGER NOT NULL,
                    PRIMARY KEY("%s", "%s"),
                    FOREIGN KEY("%s") REFERENCES "%s"("id") ON DELETE CASCADE ON UPDATE CASCADE,
                    FOREIGN KEY("%s") REFERENCES "%s"("id") ON DELETE CASCADE ON UPDATE CASCADE
                    );
                    """ % (join_table_name, id1_name, id2_name, id1_name, id2_name, id1_name, table1_name, id2_name, table2_name)

    if data is not None:
        populate_command = "INSERT INTO %s (%s, %s) VALUES (?,?)" % (join_table_name, id1_name, id2_name)
    else:
        populate_command = None
    create_sql_table(connection, create_command, populate_command, data)


def create_unique_index(connection, table_name, index_name, *fields):
    fields_str = ", ".join(fields)
    command = "CREATE UNIQUE INDEX %s ON %s ( %s )" % (index_name, table_name, fields_str)
    connection.cursor().execute(command)
    connection.commit()


def create_character_spells_table(connection):
    create_command = """
                    CREATE TABLE character_spells (
                    "character_id"	INTEGER NOT NULL,
                    "spell_id"	INTEGER NOT NULL,
                    "favorite"	INTEGER NOT NULL DEFAULT 0,
                    "known"	INTEGER NOT NULL DEFAULT 0,
                    "prepared"	INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY("character_id","spell_id"),
                    FOREIGN KEY("spell_id") REFERENCES "spells"("id") ON DELETE CASCADE ON UPDATE CASCADE,
                    FOREIGN KEY("character_id") REFERENCES "characters"("id") ON DELETE CASCADE ON UPDATE CASCADE
                );
                """
    create_sql_table(connection, create_command)


def spells_classes_data(connection, spellbook_json):

    data = []
    for spell in spellbook_json:
        name = spell["name"]
        class_names = spell["classes"]
        c = connection.cursor()
        c.execute("SELECT id FROM spells WHERE name = \"%s\"" % name)
        row = c.fetchone()
        spell_id = row[0]

        for cc in class_names:
            class_id = classes[cc]
            data.append((spell_id, class_id))

    return data
        

def main():

    # Create the database file, deleting the old one if necessary
    connection = create_db()

    # Parse the JSON
    spellbook_json = parse_json()

    # Create (and populate when applicable) the tables that are independent of the spells
    create_sources_table(connection)
    create_schools_table(connection)
    create_classes_table(connection)
    create_characters_table(connection)

    # Create and populate spells table
    create_spells_table(spellbook_json, connection)

    # Create the indices on the non-join tables
    # We do the indices first so that searching for spells by name (during the spells <-> classes table construction) will be faster
    # ID and name indices for spells, classes, sources, characters
    for table, field in product([ "spells", "classes", "sources", "characters"], [ "id", "name" ]):
        create_unique_index(connection, table, "index_%s_%s" % (table, field), field)
    
    # Code index for sources
    create_unique_index(connection, "sources", "index_sources_code", "code")

    # Create the character-based join tables
    create_simple_join_table(connection, "character_sources", "characters", "sources", "character_id", "source_id")
    create_simple_join_table(connection, "character_classes", "characters", "classes", "character_id", "class_id")
    create_character_spells_table(connection)

    # Create and populate the spells <-> classes join table
    join_data = spells_classes_data(connection, spellbook_json)
    create_simple_join_table(connection, "spell_classes", "spells", "classes", "spell_id", "class_id", join_data)

    # Create the join table indexes
    # For the character-based tables
    for item in [ "source", "class", "spell" ]:
        pluralizer = "es" if item == "class" else "s"
        table_name = "character_%s%s" % (item, pluralizer)
        index_name = "character_%s_pk_index" % item
        create_unique_index(connection, table_name, index_name, "character_id", "%s_id" % item)

    # For the spells <-> classes table
    create_unique_index(connection, "spell_classes", "spell_class_pk_index", "spell_id", "class_id")

    # Close the connection
    connection.close()

if __name__ == "__main__":
    main()