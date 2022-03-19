package dnd.jon.spellbook;

public class CharacterNameChangeDialog extends NameChangeDialog<CharacterProfile> {

    CharacterNameChangeDialog() {
        super(SpellbookViewModel::characterNameValidator,
                SpellbookViewModel::getProfileByName,
                SpellbookViewModel::renameProfile,
                R.string.name_lowercase);
    }

}
