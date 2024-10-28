package dnd.jon.spellbook;

public interface NamedItemEventHandler {
    void onUpdateEvent(String originalName);
    void onDuplicateEvent(String characterName);
    void onDeleteEvent(String name);
    void onExportEvent(String name);
    void onCopyEvent(String name);
    void onSelectionEvent(String name);
}
