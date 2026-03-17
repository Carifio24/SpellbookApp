import json
import uuid
from os.path import join

def string_for_uuid(id):
    return f"UUID.fromString(\"{id}\")"

assets_dir = join("..", "app", "src", "main", "assets")
original_filename = join(assets_dir, "Spells_en_backup.json")

uuids = {}

with open(original_filename, "r") as f:
    spells = json.load(f)

for spell in spells:
    id = spell["id"]
    new_id = str(uuid.uuid4())
    uuids[id] = new_id
    spell["id"] = new_id

output_filename = join(assets_dir, "Spells_en.json")
with open(output_filename, "w") as f:
    json.dump(spells, f, indent=4, sort_keys=True)

original_filename_pt = join(assets_dir, "Spells_pt_backup.json")
with open(original_filename_pt, "r") as f:
    spells = json.load(f)
for spell in spells:
    spell["id"] = uuids[spell["id"]]
output_filename_pt = join(assets_dir, "Spells_pt.json")
with open(output_filename_pt, "w") as f:
    json.dump(spells, f, indent=4, sort_keys=True, ensure_ascii=False)

map_filename = join(assets_dir, "Spells_uuid_map.json")
with open(map_filename, "w") as f:
    json.dump(uuids, f, indent=4, sort_keys=True)

java_map_filename = join(assets_dir, "Spells_uuid_map.java")
java_map = "static private final Map<Integer, UUID> spellUUIDMap = new HashMap<>() {{\n"
for id, new_id in uuids.items():
    java_map += f"    put({id}, {string_for_uuid(new_id)});\n"
java_map += "}};"

with open(java_map_filename, "w") as f:
    f.write(java_map)

linked_ids_filename = "2014_to_2024_ids.txt"
linked_ids = {}
java_linked_map = "static private final BidirectionalMap<UUID, UUID> spellIDLinks = new BidirectionalHashMap<>() {{\n"
with open(linked_ids_filename, "r") as f:
    for line in f:
        first, second = [int(w) for w in line.split(",")]
        first_uuid = uuids[first]
        second_uuid = uuids[second]
        java_linked_map += f"    put({string_for_uuid(first_uuid)}, {string_for_uuid(second_uuid)});\n"
java_linked_map += "}};"

linked_ids_outfile = join(assets_dir, "2014_to_2024_uuids.java")
with open(linked_ids_outfile, "w") as f:
    f.write(java_linked_map)
