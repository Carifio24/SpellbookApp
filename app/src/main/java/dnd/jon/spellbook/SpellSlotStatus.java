package dnd.jon.spellbook;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class SpellSlotStatus extends BaseObservable implements Parcelable {

    private final int[] totalSlots;
    private final int[] usedSlots;

    private static final String totalSlotsKey = "totalSlots";
    private static final String usedSlotsKey = "usedSlots";

    SpellSlotStatus(int[] totalSlots, int[] usedSlots) {
        this.totalSlots = totalSlots.clone();
        this.usedSlots = usedSlots.clone();
    }

    SpellSlotStatus() {
        this.totalSlots = new int[Spellbook.MAX_SPELL_LEVEL];
        this.usedSlots = new int[Spellbook.MAX_SPELL_LEVEL];
        Arrays.fill(this.totalSlots, 0);
        Arrays.fill(this.usedSlots, 0);
    }

    protected SpellSlotStatus(Parcel in) {
        totalSlots = in.createIntArray();
        usedSlots = in.createIntArray();
    }

    SpellSlotStatus duplicate() {
        final Parcel parcel = Parcel.obtain();
        this.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        final SpellSlotStatus status = new SpellSlotStatus(parcel);
        parcel.recycle();
        return status;
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

    // These are dummy fields that we 'update' (or tell BR that we did)
    // whenever the appropriate number of slots have changed
    private final Void totalSlotsFlag = null;
    private final Void usedSlotsFlag = null;
    @Bindable private Void getTotalSlotsFlag() { return totalSlotsFlag; }
    @Bindable private Void getUsedSlotsFlag() { return usedSlotsFlag; }

    public int getTotalSlots(int level) { return totalSlots[level-1]; }
    public int getUsedSlots(int level) { return usedSlots[level-1]; }
    public int getAvailableSlots(int level) { return totalSlots[level-1] - usedSlots[level-1]; }

    void setTotalSlots(int level, int slots) {
        totalSlots[level-1] = slots;
        notifyPropertyChanged(BR.totalSlotsFlag);
        if (slots < usedSlots[level-1]) {
            usedSlots[level-1] = slots;
            notifyPropertyChanged(BR.usedSlotsFlag);
        }
    }
    void setAvailableSlots(int level, int slots) {
        final int used = totalSlots[level-1] - slots;
        final int newAvailable = Math.max(0, used);
        if (newAvailable != usedSlots[level-1]) {
            usedSlots[level-1] = Math.max(0, used);
            notifyPropertyChanged(BR.usedSlotsFlag);
        }
    }
    void setUsedSlots(int level, int slots) {
        usedSlots[level-1] = Math.min(slots, totalSlots[level-1]);
        notifyPropertyChanged(BR.usedSlotsFlag);
    }
    void regainAllSlots() {
        Arrays.fill(usedSlots, 0);
        notifyPropertyChanged(BR.usedSlotsFlag);
    }

    void useSlot(int level) {
        final int newUsed = Math.min(usedSlots[level-1] + 1, totalSlots[level-1]);
        if (newUsed != usedSlots[level-1]) {
            usedSlots[level-1] = newUsed;
            notifyPropertyChanged(BR.usedSlotsFlag);
        }
    }
    void gainSlot(int level) {
        final int newUsed = Math.max(usedSlots[level-1] - 1, 0);
        if (newUsed != usedSlots[level-1]) {
            usedSlots[level-1] = newUsed;
            notifyPropertyChanged(BR.usedSlotsFlag);
        }
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

        final JSONArray usedSlotsJarr = new JSONArray();
        for (int slots : usedSlots) {
            usedSlotsJarr.put(slots);
        }
        json.put(usedSlotsKey, usedSlotsJarr);

        return json;
    }

    static SpellSlotStatus fromJSON(JSONObject json) throws JSONException {
        final JSONArray totalSlotsJarr = json.getJSONArray(totalSlotsKey);
        final int[] totalSlots = new int[totalSlotsJarr.length()];
        for (int i = 0; i < totalSlotsJarr.length(); i++) {
            totalSlots[i] = totalSlotsJarr.getInt(i);
        }

        final JSONArray usedSlotsJarr = json.getJSONArray(usedSlotsKey);
        final int[] usedSlots = new int[usedSlotsJarr.length()];
        for (int i = 0; i < usedSlotsJarr.length(); i++) {
            usedSlots[i] = usedSlotsJarr.getInt(i);
        }

        return new SpellSlotStatus(totalSlots, usedSlots);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeIntArray(totalSlots);
        parcel.writeIntArray(usedSlots);
    }
}
