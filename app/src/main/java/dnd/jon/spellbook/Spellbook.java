package dnd.jon.spellbook;

import java.util.ArrayList;

public class Spellbook {

    static final String[] schoolNames = {"Abjuration", "Conjuration", "Divination", "Enchantment", "Evocation", "Illusion", "Necromancy", "Transmutation"};
    static final String[] casterNames = {"Bard", "Cleric", "Druid", "Paladin", "Ranger", "Sorcerer", "Warlock", "Wizard"};
    static final String[] subclassNames = {"Berserker", "Devotion", "Draconic", "Evocation", "Fiend", "Hunter", "Land", "Life", "Lore", "Open Hand", "Thief"};
    static final String[] sourcebookCodes = {"PHB", "XGE", "SCAG"};

    static final int N_SCHOOLS = schoolNames.length;
    static final int N_CASTERS = casterNames.length;
    static final int N_SUBCLASSES = subclassNames.length;

    ArrayList<Spell> spells;
    int N_SPELLS;

    Spellbook(String jsonStr) {
        try {
            spells = SpellParser.parseSpellList(jsonStr);
            N_SPELLS = spells.size();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setSpells(ArrayList<Spell> inSpells) {
        spells = inSpells;
    }

}
