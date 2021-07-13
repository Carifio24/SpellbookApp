package dnd.jon.spellbook;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SpellSlotStatus implements Parcelable {

    private final int[] totalSlots;
    private final int[] availableSlots;

    private static final String totalSlotsKey = "totalSlots";
    private static final String availableSlotsKey = "availableSlots";

    SpellSlotStatus(int[] totalSlots, int[] availableSlots) {
        this.totalSlots = totalSlots.clone();
        this.availableSlots = availableSlots.clone();
    }

    SpellSlotStatus() {
        this.totalSlots = new int[Spellbook.MAX_SPELL_LEVEL+1];
        this.availableSlots = new int[Spellbook.MAX_SPELL_LEVEL+1];
    }

    protected SpellSlotStatus(Parcel in) {
        totalSlots = in.createIntArray();
        availableSlots = in.createIntArray();
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
