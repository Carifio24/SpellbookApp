package dnd.jon.spellbook;

import android.os.Parcel;
import android.os.Parcelable;

public class SpellSlotStatus implements ParcelReady, Parcelable {

    final String name;
    final int[] totalSlots;
    final int[] availableSlots;

    SpellSlotStatus(CharacterProfile cp) {
        this.name = cp.getName();
        this.totalSlots = cp.getTotalSlots();
        this.availableSlots = cp.getAvailableSlots();
    }

    protected SpellSlotStatus(Parcel in) {
        this.name = in.readString();
        this.totalSlots = in.createIntArray();
        this.availableSlots = in.createIntArray();
    }

    public static final Creator<SpellSlotStatus> CREATOR = new ParcelCreator<>(SpellSlotStatus.class, SpellSlotStatus::new);

    @Override public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeIntArray(totalSlots);
        dest.writeIntArray(availableSlots);
    }
}
