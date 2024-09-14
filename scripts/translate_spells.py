# Note that we need to use googletrans==3.1.0a0
# https://github.com/ssut/py-googletrans/issues/280#issue-827952964

from json import load, dump
from os.path import splitext
from typing import TypeVar

from googletrans import Translator

NON_TRANSLATED_KEYS = {
    "level",
    "concentration",
    "ritual",
    "id",
    "components",
}

CLASSES = {
    "Artificer",
    "Bard",
    "Cleric",
    "Druid",
    "Paladin",
    "Ranger",
    "Sorcerer",
    "Wizard",
    "Warlock",
}

SCHOOLS = {
    "Abjuration",
    "Conjuration",
    "Divination",
    "Enchantment",
    "Evocation",
    "Illusion",
    "Necromancy",
    "Transmutation",
}

CASTING_TIMES = {
    "1 action",
    "1 bonus action",
    "1 reaction",
    "Action",
    "Bonus action",
    "Reaction",
}

PRETRANSLATE = CLASSES.union(SCHOOLS).union(CASTING_TIMES)


# Strings are the only things we want to translate
# For our simple use case we can assume that everything 
# that we'll see is a string, int, dict, or list
T = TypeVar('T', str, dict, list, int)
def translate_item(item: T,
                   language: str,
                   translator: Translator,
                   pretranslated: dict[str, str]) -> T:
    if isinstance(item, str):
        if item in pretranslated:
            return pretranslated[item]
        while True:
            try:
                return translator.translate(item, dest=language, src="en").text
            except:
                print(f"Retrying {item}")
    elif isinstance(item, list):
        return [translate_item(t, language, translator, pretranslated) for t in item]
    elif isinstance(item, dict):
        return { key: translate_item(value, language, translator, pretranslated) for key, value in item.items() }
    elif isinstance(item, int):
        return item


def translate_spell(spell: dict,
                    language: str,
                    translator: Translator,
                    pretranslated: dict[str, str]) -> dict:
    print(f"Translating {spell['name']}")
    return {
        key: value if key in NON_TRANSLATED_KEYS else translate_item(value, language, translator, pretranslated)
        for key, value in spell.items()
    }


def translate_file(filepath: str, language: str):

    with open(filepath, 'r') as f:
        spells = load(f)


    translator = Translator()

    # There are a lot of strings like schools and classes that will pop up
    # over and over again. So we only translate them once and then reuse the
    # translations throughout
    pretranslated = {
        item: translate_item(item, language, translator, {})
        for item in PRETRANSLATE
    }
    translated_spells = [translate_spell(spell, language, translator, pretranslated) for spell in spells]

    base, ext = splitext(filepath)
    if base.endswith("_en"):
        base = base[:-3]

    output_path = f"{base}_{language}{ext}"
    print(output_path)
    with open(output_path, 'w', encoding="utf8") as f:
        dump(translated_spells, f,
             indent=4, sort_keys=True,
             ensure_ascii=False)


if __name__ == "__main__":
    from argparse import ArgumentParser
    parser = ArgumentParser(prog="Translate Spells",
                            description="Translate a file of spells from English \
                                         to a specified language")
    parser.add_argument("-f", "--file", type=str)
    parser.add_argument("-l", "--lang", type=str)

    args = parser.parse_args()
    translate_file(args.file, args.lang)
