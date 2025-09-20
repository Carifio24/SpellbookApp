package dnd.jon.spellbook;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpellFilterStatus extends BaseObservable implements Parcelable {

    private static final String spellsKey = "Spells";
    private static final String spellIDKey = "SpellID";
    private static final String favoriteKey = "Favorite";
    private static final String preparedKey = "Prepared";
    private static final String knownKey = "Known";

    private final Map<UUID, SpellStatus> spellStatusMap;

    SpellFilterStatus(Map<UUID,SpellStatus> spellStatusMap) {
        this.spellStatusMap = spellStatusMap;
    }

    SpellFilterStatus() {
        this.spellStatusMap = new HashMap<>();
    }

    protected SpellFilterStatus(Parcel in) {
        this.spellStatusMap = new HashMap<>();
        int size = in.readInt();
        for (int i = 0; i < size; i++){
            final UUID id = (UUID) in.readSerializable();
            final SpellStatus status = in.readParcelable(SpellStatus.class.getClassLoader());
            spellStatusMap.put(id, status);
        }
    }

    public static final Creator<SpellFilterStatus> CREATOR = new Creator<SpellFilterStatus>() {
        @Override
        public SpellFilterStatus createFromParcel(Parcel in) {
            return new SpellFilterStatus(in);
        }

        @Override
        public SpellFilterStatus[] newArray(int size) {
            return new SpellFilterStatus[size];
        }
    };

    SpellFilterStatus duplicate() {
        final Parcel parcel = Parcel.obtain();
        this.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        final SpellFilterStatus sfs = new SpellFilterStatus(parcel);
        parcel.recycle();
        return sfs;
    }

    private boolean isProperty(Spell spell, Function<SpellStatus,Boolean> property) {
        if (spell == null) { return false; }
        if (spellStatusMap.containsKey(spell.getID())) {
            SpellStatus status = spellStatusMap.get(spell.getID());
            return property.apply(status);
        }
        return false;
    }

    boolean hiddenByFilter(Spell spell, StatusFilterField sff) {
        switch (sff) {
            case FAVORITES:
                return !isFavorite(spell);
            case PREPARED:
                return !isPrepared(spell);
            case KNOWN:
                return !isKnown(spell);
            default:
                return false;
        }
    }

    SpellStatus getStatus(UUID spellID) { return spellStatusMap.get(spellID); }
    SpellStatus getStatus(Spell spell) { return getStatus(spell.getID()); }
    boolean isFavorite(Spell spell) { return isProperty(spell, (SpellStatus status) -> status.favorite); }
    boolean isPrepared(Spell spell) { return isProperty(spell, (SpellStatus status) -> status.prepared); }
    boolean isKnown(Spell spell) { return isProperty(spell, (SpellStatus status) -> status.known); }

    private Collection<UUID> spellIDsByProperty(Function<SpellStatus,Boolean> property) {
        return spellStatusMap.entrySet().stream().filter(entry -> property.apply(entry.getValue())).map(Map.Entry::getKey).collect(Collectors.toList());
    }
    Collection<UUID> favoriteSpellIDs() { return spellIDsByProperty(ss -> ss.favorite); }
    Collection<UUID> preparedSpellIDs() { return spellIDsByProperty(ss -> ss.prepared); }
    Collection<UUID> knownSpellIDs() { return spellIDsByProperty(ss -> ss.known); }
    Collection<UUID> spellIDsWithOneProperty() {
        return spellStatusMap.entrySet().stream().filter(entry -> {
            final SpellStatus status = entry.getValue();
            return status.favorite || status.prepared || status.known;
        }).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    // Dummy field that we can tell BR has updated whenever we set a property
    private final Void spellFilterFlag = null;
    @Bindable private Void getSpellFilterFlag() { return spellFilterFlag; }

    // Setting whether a spell is on a given spell list
    private void setProperty(Spell spell, Boolean val, BiConsumer<SpellStatus,Boolean> propSetter) {
        final UUID spellID = spell.getID();
        if (spellStatusMap.containsKey(spellID)) {
            SpellStatus status = spellStatusMap.get(spellID);
            propSetter.accept(status, val);
            // spellStatuses.put(spellName, status);
            if (status != null && status.noneTrue()) { // We can remove the key if all three are false
                spellStatusMap.remove(spellID);
            }
        } else if (val) { // If the key doesn't exist, we only need to modify if val is true
            SpellStatus status = new SpellStatus();
            propSetter.accept(status, true);
            spellStatusMap.put(spellID, status);
        }
        notifyPropertyChanged(BR.spellFilterFlag);
    }
    void setFavorite(Spell s, Boolean fav) { setProperty(s, fav, (SpellStatus status, Boolean tf) -> status.favorite = tf); }
    void setPrepared(Spell s, Boolean prep) { setProperty(s, prep, (SpellStatus status, Boolean tf) -> status.prepared = tf); }
    void setKnown(Spell s, Boolean known) { setProperty(s, known, (SpellStatus status, Boolean tf) -> status.known = tf); }

    // Toggling whether a given property is set for a given spell
//    private void toggleProperty(Spell s, Function<SpellStatus,Boolean> property, BiConsumer<SpellStatus,Boolean> propSetter) { setProperty(s, !isProperty(s, property), propSetter); }
//    void toggleFavorite(Spell s) { toggleProperty(s, (SpellStatus status) -> status.favorite, (SpellStatus status, Boolean tf) -> status.favorite = tf); }
//    void togglePrepared(Spell s) { toggleProperty(s, (SpellStatus status) -> status.prepared, (SpellStatus status, Boolean tf) -> status.prepared = tf); }
//    void toggleKnown(Spell s) { toggleProperty(s, (SpellStatus status) -> status.known, (SpellStatus status, Boolean tf) -> status.known = tf); }

    static SpellFilterStatus fromJSON(JSONObject json) throws JSONException {
        final Map<Integer,SpellStatus> spellStatusMap = new HashMap<>();
        final JSONArray jsonArray = json.getJSONArray(spellsKey);
        for (int i = 0; i < jsonArray.length(); ++i) {
            final JSONObject jsonObject = jsonArray.getJSONObject(i);

            // Get the name and array of statuses
            final Integer spellID = jsonObject.getInt(spellIDKey);

            // Load the spell statuses
            final boolean fav = jsonObject.getBoolean(favoriteKey);
            final boolean prep = jsonObject.getBoolean(preparedKey);
            final boolean known = jsonObject.getBoolean(knownKey);
            final SpellStatus status = new SpellStatus(fav, prep, known);

            // Add to the map
            spellStatusMap.put(spellID, status);
        }

        return new SpellFilterStatus(spellStatusMap);
    }

    JSONObject toJSON() throws JSONException {
        final JSONObject json = new JSONObject();
        JSONArray spellStatusJA = new JSONArray();
        for (HashMap.Entry<Integer, SpellStatus> data : spellStatusMap.entrySet()) {
            JSONObject statusJSON = new JSONObject();
            statusJSON.put(spellIDKey, data.getKey());
            SpellStatus status = data.getValue();
            statusJSON.put(favoriteKey, status.favorite);
            statusJSON.put(preparedKey, status.prepared);
            statusJSON.put(knownKey, status.known);
            spellStatusJA.put(statusJSON);
        }
        json.put(spellsKey, spellStatusJA);
        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(spellStatusMap.size());
        for (Map.Entry<Integer,SpellStatus> entry : spellStatusMap.entrySet()) {
            parcel.writeInt(entry.getKey());
            parcel.writeParcelable(entry.getValue(), 0);
        }
    }
}
