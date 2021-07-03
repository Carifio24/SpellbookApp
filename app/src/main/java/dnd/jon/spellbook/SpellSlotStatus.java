package dnd.jon.spellbook;

import org.parceler.Parcel;

@Parcel
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
