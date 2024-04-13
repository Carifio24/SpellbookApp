package dnd.jon.spellbook;

import java.io.File;
import java.util.Collection;

interface SpellListExporter {
    SpellListExporter setTitle(String title);
    SpellListExporter addSpell(Spell spell);
    SpellListExporter addSpells(Collection<Spell> spells);
    boolean export(File filepath);
}
