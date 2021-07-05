package dnd.jon.spellbook;

public class SpellSlotStatus {

    final String name;
    final int[] totalSlots;
    final int[] availableSlots;

    SpellSlotStatus(CharacterProfile cp) {
        this.name = cp.getName();
        this.totalSlots = cp.getTotalSlots();
        this.availableSlots = cp.getAvailableSlots();
    }
}
