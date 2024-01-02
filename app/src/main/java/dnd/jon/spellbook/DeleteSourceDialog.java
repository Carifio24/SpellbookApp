package dnd.jon.spellbook;

public class DeleteSourceDialog extends DeleteNamedItemDialog {
    public DeleteSourceDialog() {
        super(R.string.source, SpellbookViewModel::deleteSourceByName);
    }
}
