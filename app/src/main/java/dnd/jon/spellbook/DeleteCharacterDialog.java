package dnd.jon.spellbook;

public class DeleteCharacterDialog extends DeleteNamedItemDialog {
    public DeleteCharacterDialog() {
        super(R.string.character, SpellbookViewModel::deleteProfileByName);
    }
}
