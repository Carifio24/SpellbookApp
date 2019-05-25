package dnd.jon.spellbook;

import org.json.JSONArray;

import java.util.ArrayList;

public class Spellbook {

    static final String[] schoolNames = {"Abjuration", "Conjuration", "Divination", "Enchantment", "Evocation", "Illusion", "Necromancy", "Transmutation"};
    static final String[] casterNames = {"Bard", "Cleric", "Druid", "Paladin", "Ranger", "Sorcerer", "Warlock", "Wizard"};
    static final String[] subclassNames = {"Berserker", "Devotion", "Draconic", "Evocation", "Fiend", "Hunter", "Land", "Life", "Lore", "Open Hand", "Thief"};
    static final String[] sourcebookCodes = {"PHB", "XGE", "SCAG"};

    ArrayList<Spell> spells;

    Spellbook(JSONArray jarr) {
        try {
            spells = SpellParser.parseSpellList(jarr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setSpells(ArrayList<Spell> inSpells) {
        spells = inSpells;
    }

}
