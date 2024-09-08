from bs4 import BeautifulSoup, Tag
import json
import re


filename = "2024Spells.html"
with open(filename, 'r') as f:
    html = f.read()

WORDS = "[a-zA-Z0-9, ]+"
LEVEL_REGEX = re.compile(f"Level ([0-9]) ({WORDS}) \\(({WORDS})\\)")
CANTRIP_REGEX = re.compile(f"([a-zA-Z]+) Cantrip \\(({WORDS})\\)")
CASTING_TIME_REGEX = re.compile(f"Casting Time: ({WORDS})")
COMPONENTS_REGEX = re.compile(f"Components?: ({WORDS})(?:\\(({WORDS})\\))?")
DURATION_REGEX = re.compile(f"Duration: ({WORDS})")
RANGE_REGEX = re.compile(f"Range: ({WORDS})")
CANTRIP_UPGRADE = "Cantrip Upgrade. "
HIGHER_LEVEL = "Using a Higher-Level Spell Slot. "
CONCENTRATION = "Concentration, "


id = 531
spells = []
soup = BeautifulSoup(html, "html.parser")


def make_new_spell(id: int):
    return {
        "desc": [],
        "id": id,
        "locations": [{ "sourcebook": "PHB24", "page": 239 }]
    }


def finalize_spell(spell: dict):
    spell["desc"] = "\n\n".join(spell["desc"]).replace("\u2019", "'")



def is_separator(element: Tag):
    return element.name == "hr" and element["class"] == ["separator"]


# Tables are a little tricky - there are times when we might want to
# combine two tables, or need to adjust the spacing based on the length of
# the content in the cells. But this at least gives us a start
def handle_table(element: Tag):
    global spell
    row_strings = []
    head = element.thead
    if head is not None:
        header_items = [item.text for item in head.find_all(name="th")]
        row_strings.append("  ".join(header_items))
    body = element.tbody
    if body is not None:
        for row in body.find_all(name="tr"):
            row_items = [item.text for item in row.find_all(name="td")]
            row_strings.append("  ".join(row_items))
    
    table_text = "\n".join(row_strings)
    spell["desc"].append(table_text)


def handle_element(element: Tag):
    global spell
    match element.name:
        case "h3":
            spell["name"] = element.text
        case "p":
            text = element.text
            if (match := LEVEL_REGEX.match(text)) is not None:
                spell["level"] = int(match.group(1))
                spell["school"] = match.group(2)
                spell["classes"] = match.group(3).replace(" ", "").split(",")
                return
            
            if (match := CANTRIP_REGEX.match(text)) is not None:
                spell["level"] = 0
                spell["school"] = match.group(1)
                spell["classes"] = match.group(2).replace(" ", "").split(",")
                return 

            if (match := CASTING_TIME_REGEX.match(text)) is not None:
                spell["casting_time"] = match.group(1)
                return 

            if (match := DURATION_REGEX.match(text)) is not None:
                duration_text = match.group(1)
                if duration_text.startswith(CONCENTRATION):
                    duration_text = duration_text[len(CONCENTRATION):]
                    spell["concentration"] = True
                spell["duration"] = duration_text
                return 

            if (match := RANGE_REGEX.match(text)) is not None:
                spell["range"] = match.group(1)
                return 

            if (match := COMPONENTS_REGEX.match(text)) is not None:
                spell["components"] = match.group(1).replace(" ", "").split(",")
                if match.group(2):
                    spell["material"] = match.group(2).capitalize()
                return 

            if text.startswith(CANTRIP_UPGRADE):
                spell["higher_level"] = text[len(CANTRIP_UPGRADE):]
                return 

            if text.startswith(HIGHER_LEVEL):
                spell["higher_level"] = text[len(HIGHER_LEVEL):]
                return 

            spell["desc"].append(text)
        case "table":
            handle_table(element)
        case "div":
            for child in element:
                if isinstance(child, Tag):
                    handle_element(child)

        case _:
            pass


spell = make_new_spell(id)
for element in soup:
    if not isinstance(element, Tag):
        continue

    if is_separator(element):
        finalize_spell(spell)
        spells.append(spell)
        id += 1
        spell = make_new_spell(id)
        continue

    handle_element(element)
 

finalize_spell(spell)
spells.append(spell)

with open("2024_spells.json", "w") as f:
    json.dump(spells, f, indent=4, sort_keys=True)
