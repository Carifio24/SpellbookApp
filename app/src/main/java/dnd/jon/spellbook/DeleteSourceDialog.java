package dnd.jon.spellbook;

public class DeleteSourceDialog extends DeleteNamedItemDialog {

    private static boolean deleteSource(SpellbookViewModel viewModel, String name) {
        final Source source = Source.fromInternalName(name);
        return viewModel.deleteSourceByNameOrCode(source.getCode());
    }

    // We want the name to be what gets used in the dialog text
    // but we actually store created sources by code,
    // so we need this intermediate function
    public DeleteSourceDialog() {
        super(R.string.source, DeleteSourceDialog::deleteSource);
    }
}
