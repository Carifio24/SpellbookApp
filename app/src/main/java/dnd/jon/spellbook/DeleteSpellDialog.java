package dnd.jon.spellbook;

public class DeleteSpellDialog extends DeleteNamedItemDialog {
    public DeleteSpellDialog() {
        super(R.string.spell, SpellbookViewModel::deleteSpellByName);
    }
}
