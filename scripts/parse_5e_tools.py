from argparse import ArgumentParser
from bs4 import BeautifulSoup
import json
from pathlib import Path
import re
from uuid import uuid4


NONZERO_LEVEL_SCHOOL_PATTERN = r"Level ([1-9]) ([a-zA-Z]+)"
CANTRIP_SCHOOL_PATTERN = r"([a-zA-Z]+) Cantrip"
LEVEL_SCHOOL_PATTERN = re.compile(f"(?:{NONZERO_LEVEL_SCHOOL_PATTERN})|(?:{CANTRIP_SCHOOL_PATTERN})")
print(LEVEL_SCHOOL_PATTERN)
CONCENTRATION_PREFIX = "Concentration, up to "
RITUAL_ENDING = " or Ritual"
HIGHER_LEVEL_PREFIX_1 = "Using a Higher-Level Spell Slot. "
HIGHER_LEVEL_PREFIX_2 = "Using a Higher-Level Spell Slot. "

CASTING_TIME_PATTERN = re.compile("Casting Time: (.+)")
RANGE_PATTERN = re.compile("Range: (.+)")
DURATION_PATTERN = re.compile("Duration: (.+)")
COMPONENTS_PATTERN = re.compile("Components: (.+)")
PATTERNS = {
    "casting_time": CASTING_TIME_PATTERN,
    "components": COMPONENTS_PATTERN,
    "duration": DURATION_PATTERN,
    "range": RANGE_PATTERN,
}

COMPONENTS_PARSE_PATTERN = re.compile(r"([VSM, ]+) ?(\(.+\))?")

current_dir = Path(__file__).resolve().parent


def parse_5e_tools_spells(
    html_path,
    sourcebook_code,
    output_path,
):

    with open(html_path, 'r') as f:
        html = f.read()
    
    soup = BeautifulSoup(html, features="html.parser")
    
    spell_cards = soup.select(".bkmv__wrp-item")
    
    spells = []
    for card in spell_cards:
    
        spell = { "id": str(uuid4()) }
        
        name_el = card.select_one(".ve-stats__h-name")
        if name_el is None:
            name_el = card.select_one("[data-rd-data-embed-name]")
        spell["name"] = name_el.text
    
        level_school_el = card.select_one("div.ve-pb-2 i")
        level_school_text = level_school_el.text
        m = LEVEL_SCHOOL_PATTERN.match(level_school_text)
        cantrip = not m.group(1)
        print(m, m.group(0), m.group(1), m.group(3))
        spell["level"] = int(m.group(1)) if not cantrip else 0
        school = m.group(3 if cantrip else 2)
        spell["school"] = school
    
        block = card.select("div.ve-pb-2")[1]
        cols = block.select("div.ve-flex-col")
    
        casting_time_el, components_el = cols[0].find_all("div")
        range_el, duration_el = cols[1].find_all("div")
    
        elements = {
            "casting_time": casting_time_el,
            "components": components_el,
            "range": range_el,
            "duration": duration_el,
        }
        for field, element in elements.items():
            m = PATTERNS[field].match(element.text)
            spell[field] = m.group(1)
    
        if spell["casting_time"].endswith(RITUAL_ENDING):
            spell["casting_time"] = spell["casting_time"][:-len(RITUAL_ENDING)]
            spell["ritual"] = True
    
        if spell["duration"].startswith(CONCENTRATION_PREFIX):
            spell["duration"] = spell["duration"][len(CONCENTRATION_PREFIX):]
            spell["concentration"] = True
    
        components = spell["components"]
        m = COMPONENTS_PARSE_PATTERN.match(components)
        if m is not None:
            material = m.group(2)
            if material is not None:
                material = material.strip()
                material = material[1].upper() + material[2:-1]
                spell["material"] = material
            components = m.group(1)
        components = components.strip()
        spell["components"] = [c for c in re.split(", ?", components) if c]
    
        description_el = card.select_one(".ve-rd__b--2")
        paragraphs = description_el.find_all("p")
        spell["desc"] = "\n\n".join([p.text for p in paragraphs])
    
        higher_level_el = card.select_one(".ve-rd__b--3")
        if higher_level_el is not None:
            higher_level_text = higher_level_el.text
            prefix = None
            if higher_level_text.startswith(HIGHER_LEVEL_PREFIX_1):
                prefix = HIGHER_LEVEL_PREFIX_1
            elif higher_level_text.startswith(HIGHER_LEVEL_PREFIX_2):
                prefix = HIGHER_LEVEL_PREFIX_2
            if prefix is not None:
                spell["higher_level"] = higher_level_text[len(prefix):]
    
        page_el = card.select_one(".ve-rd__stats-name-page")
        spell["locations"] = [{ "sourcebook": sourcebook_code, "page": int(page_el.text[1:]) }]
    
        table = card.select_one("td.ve-pb-2")
        classes_row = table.select_one(":scope > div:last-of-type")
        classes_row_subitems = classes_row.find_all("span")
        classes = []
        if classes_row_subitems:
            classes_header = classes_row_subitems[0]
            if not classes_header.text.startswith("Classes:"):
                classes_elements = classes_row.find_all("span")[1:]
                classes = set(text for el in classes_elements if (text := el.text) != ", ")
        spell["classes"] = list(classes)
    
        spells.append(spell)
    
    with open(output_path, 'w') as f:
        json.dump(spells, f, sort_keys=True, ensure_ascii=True, indent=4)


if __name__ == "__main__":
    parser = ArgumentParser(
        prog="5e Tools Parser",
        description="Parse spells from 5e Tools' HTML format",
    )
    parser.add_argument("html_path")
    parser.add_argument("sourcebook_code")
    parser.add_argument("output_path")

    args = parser.parse_args()
    parse_5e_tools_spells(args.html_path, args.sourcebook_code, args.output_path)
