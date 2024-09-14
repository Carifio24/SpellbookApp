from bs4 import BeautifulSoup, Tag
from itertools import accumulate
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

STARTING_PAGE = 239
SPELLS_PER_PAGE = (5, 4, 2, 5, 3, 5, 7, 2, 5,
                   5, 6, 3, 5, 5, 2, 4, 5, 4,
                   1, 5, 5, 3, 5, 5, 2, 3, 5,
                   5, 3, 5, 2, 3, 6, 2, 3, 3, 
                   5, 5, 5, 2, 4, 3, 3, 4, 4,
                   6, 3, 3, 5, 4, 3, 4, 3, 5,
                   5, 1, 4, 6, 2, 6, 5, 3, 1,
                   5, 4, 4, 3, 6, 3, 2, 4, 3,
                   6, 6, 4, 4, 2, 7, 4, 5, 5,
                   3, 5, 2, 1, 2, 1, 2, 0, 1,
                   4, 2, 4, 2, 3, 6, 2, 5, 3,
                   2, 3, 4, 4, 0, 6)



STARTING_ID = 531
FIRST_SPELL_PER_PAGE = tuple(accumulate(SPELLS_PER_PAGE, initial=STARTING_ID))

id = STARTING_ID
spells = []
soup = BeautifulSoup(html, "html.parser")


def page_for_spell(id: int) -> int:
    page_offset = next((index-1 for index, last_id in enumerate(FIRST_SPELL_PER_PAGE) if last_id > id), len(SPELLS_PER_PAGE))
    return STARTING_PAGE + page_offset


def make_new_spell(id: int) -> dict:
    return {
        "desc": [],
        "id": id,
        "locations": [{ "sourcebook": "PHB24", "page": page_for_spell(id) }],
        "ruleset": "2024",
    }


def finalize_spell(spell: dict):
    spell["desc"] = "\n\n".join(spell["desc"]).replace("\u2019", "'")


def is_separator(element: Tag) -> bool:
    return (element.name == "hr" and element["class"] == ["separator"]) \
        or \
           (element.name == "h2" and element["class"] == ["compendium-hr", "heading-anchor"])


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
        if "name" in spell:
            spells.append(spell)
            id += 1
        spell = make_new_spell(id)
        continue

    handle_element(element)
 

finalize_spell(spell)
spells.append(spell)

with open("2024_spells.json", "w") as f:
    json.dump(spells, f, indent=4, sort_keys=True)
