package dnd.jon.spellbook;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SpellSlotStatus extends BaseObservable implements Parcelable {

    private final int[] totalSlots;
    private final int[] availableSlots;

    private static final String totalSlotsKey = "totalSlots";
    private static final String availableSlotsKey = "availableSlots";

    SpellSlotStatus(int[] totalSlots, int[] availableSlots) {
        this.totalSlots = totalSlots.clone();
        this.availableSlots = availableSlots.clone();
    }

    SpellSlotStatus() {
        this.totalSlots = new int[Spellbook.MAX_SPELL_LEVEL];
        this.availableSlots = new int[Spellbook.MAX_SPELL_LEVEL];
    }

    protected SpellSlotStatus(Parcel in) {
        totalSlots = in.createIntArray();
        availableSlots = in.createIntArray();
    }

    SpellSlotStatus duplicate() {
        final Parcel parcel = Parcel.obtain();
        this.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        final SpellSlotStatus sss = new SpellSlotStatus(parcel);
        parcel.recycle();
        return sss;
    }

    public static final Creator<SpellSlotStatus> CREATOR = new Creator<SpellSlotStatus>() {
        @Override
        public SpellSlotStatus createFromParcel(Parcel in) {
            return new SpellSlotStatus(in);
        }

        @Override
        public SpellSlotStatus[] newArray(int size) {
            return new SpellSlotStatus[size];
        }
    };

    public int getTotalSlots(int level) { return totalSlots[level-1]; }
    int getAvailableSlots(int level) { return availableSlots[level-1]; }
    int getUsedSlots(int level) { return totalSlots[level-1] - availableSlots[level-1]; }

    void setTotalSlots(int level, int slots) { totalSlots[level-1] = slots; notifyChange(); notifyAll(); }
    void setAvailableSlots(int level, int slots) { availableSlots[level-1] = Math.min(slots, totalSlots[level-1]); }
    void refillAllSlots() { System.arraycopy(totalSlots, 0, availableSlots, 0, totalSlots.length); }

    void useSlot(int level) { availableSlots[level-1] -= 1; }
    void gainSlot(int level) {
        availableSlots[level-1] += Math.min(totalSlots[level-1], availableSlots[level-1] + 1);
    }

    int maxLevelWithSlots() {
        for (int level = Spellbook.MAX_SPELL_LEVEL; level > 0; level--) {
            if (getTotalSlots(level) > 0) {
                return level;
            }
        }
        return 0;
    }


    JSONObject toJSON() throws JSONException {
        final JSONObject json = new JSONObject();

        final JSONArray totalSlotsJarr = new JSONArray();
        for (int slots : totalSlots) {
            totalSlotsJarr.put(slots);
        }
        json.put(totalSlotsKey, totalSlotsJarr);

        final JSONArray availableSlotsJarr = new JSONArray();
        for (int slots : availableSlots) {
            availableSlotsJarr.put(slots);
        }
        json.put(availableSlotsKey, availableSlotsJarr);

        return json;
    }

    static SpellSlotStatus fromJSON(JSONObject json) throws JSONException {
        final JSONArray totalSlotsJarr = json.getJSONArray(totalSlotsKey);
        final int[] totalSlots = new int[totalSlotsJarr.length()];
        for (int i = 0; i < totalSlotsJarr.length(); i++) {
            totalSlots[i] = totalSlotsJarr.getInt(i);
        }

        final JSONArray availableSlotsJarr = json.getJSONArray(availableSlotsKey);
        final int[] availableSlots = new int[availableSlotsJarr.length()];
        for (int i = 0; i < availableSlotsJarr.length(); i++) {
            availableSlots[i] = availableSlotsJarr.getInt(i);
        }

        return new SpellSlotStatus(totalSlots, availableSlots);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeIntArray(totalSlots);
        parcel.writeIntArray(availableSlots);
    }
}
