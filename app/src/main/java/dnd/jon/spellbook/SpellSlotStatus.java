package dnd.jon.spellbook;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel
public class SpellSlotStatus {

    private final int[] totalSlots;
    private final int[] availableSlots;

    private static final String totalSlotsKey = "totalSlots";
    private static final String availableSlotsKey = "availableSlots";

    @ParcelConstructor
    SpellSlotStatus(int[] totalSlots, int[] availableSlots) {
        this.totalSlots = totalSlots.clone();
        this.availableSlots = availableSlots.clone();
    }

    SpellSlotStatus() {
        this.totalSlots = new int[Spellbook.MAX_SPELL_LEVEL+1];
        this.availableSlots = new int[Spellbook.MAX_SPELL_LEVEL+1];
    }

    int getTotalSlots(int level) { return totalSlots[level]; }
    int getAvailableSlots(int level) { return availableSlots[level]; }
    int getUsedSlots(int level) { return totalSlots[level] - availableSlots[level]; }

    void setTotalSlots(int level, int slots) { totalSlots[level] = slots; }
    void setAvailableSlots(int level, int slots) { availableSlots[level] = Math.min(slots, totalSlots[level]); }
    void refillAllSlots() { System.arraycopy(totalSlots, 0, availableSlots, 0, totalSlots.length); }

    void useSlot(int level) { availableSlots[level] -= 1; }
    void gainSlot(int level) {
        totalSlots[level] += 1;
        availableSlots[level] += 1;
    }
}
