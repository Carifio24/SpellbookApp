package dnd.jon.spellbook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

class CharacterProfile {

    // The map of spell statuses
    private String charName;
    private HashMap<String, SpellStatus> spellStatuses;

    // Keys for loading/saving
    static private String charNameKey = "CharacterName";
    static private String spellsKey = "Spells";
    static private String spellNameKey = "SpellName";
    static private String favoriteKey = "Favorite";
    static private String preparedKey = "Prepared";
    static private String knownKey = "Known";


    CharacterProfile(String name, HashMap<String, SpellStatus> spellStatusesIn) {
        charName = name;
        spellStatuses = spellStatusesIn;
    }

    CharacterProfile(String nameIn) {
        this(nameIn, new HashMap<>());
    }

    // Getters
    String getName() { return charName; }
    HashMap<String, SpellStatus> getStatuses() { return spellStatuses; }

    // Save to JSON
    JSONObject toJSON() throws JSONException {

        // The JSON object
        JSONObject json = new JSONObject();

        // Store the data
        json.put(charNameKey, charName);
        JSONArray spellStatusJA = new JSONArray();
        for (HashMap.Entry<String, SpellStatus> data : spellStatuses.entrySet()) {
            JSONObject statusJSON = new JSONObject();
            statusJSON.put(spellNameKey, data.getKey());
            SpellStatus status = data.getValue();
            statusJSON.put(favoriteKey, status.favorite);
            statusJSON.put(preparedKey, status.prepared);
            statusJSON.put(knownKey, status.known);
            spellStatusJA.put(statusJSON);
            System.out.println(statusJSON);
        }
        json.put(spellsKey, spellStatusJA);

        return json;
    }

    private boolean isProperty(Spell s, Function<SpellStatus,Boolean> property) {
        if (spellStatuses.containsKey(s.getName())) {
            SpellStatus status = spellStatuses.get(s.getName());
            return property.apply(status);
        }
        return false;
    }

    boolean isFavorite(Spell s) {
        return isProperty(s, (SpellStatus status) -> status.favorite);
    }

    boolean isPrepared(Spell s) {
        return isProperty(s, (SpellStatus status) -> status.prepared);
    }

    boolean isKnown(Spell s) {
        return isProperty(s, (SpellStatus status) -> status.known);
    }

    private void setProperty(Spell s, Boolean val, BiConsumer<SpellStatus,Boolean> propSetter) {
        String spellName = s.getName();
        if (spellStatuses.containsKey(spellName)) {
            SpellStatus status = spellStatuses.get(spellName);
            propSetter.accept(status, val);
            // spellStatuses.put(spellName, status);
            if (status.noneTrue()) { // We can remove the key if all three are false
                spellStatuses.remove(spellName);
            }
        } else if (val) { // If the key doesn't exist, we only need to modify if val is true
            SpellStatus status = new SpellStatus();
            propSetter.accept(status, val);
            spellStatuses.put(spellName, status);
        }
    }

    void setFavorite(Spell s, Boolean fav) {
        setProperty(s, fav, (SpellStatus status, Boolean tf) -> {status.favorite = tf;});
    }

    void setPrepared(Spell s, Boolean prep) {
        setProperty(s, prep, (SpellStatus status, Boolean tf) -> {status.prepared = tf;});
    }

    void setKnown(Spell s, Boolean known) {
        setProperty(s, known, (SpellStatus status, Boolean tf) -> {status.known = tf;});
    }

    // Save to a file
    void save(File filename) {
        try {
            JSONObject cpJSON = toJSON();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
                bw.write(cpJSON.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // Load from JSON
    static CharacterProfile fromJSON(JSONObject json) throws JSONException {

        // The map
        HashMap<String, SpellStatus> spellStatusMap = new HashMap<>();

        String charName = json.getString(charNameKey);

        JSONArray jarr = json.getJSONArray(spellsKey);
        for (int i = 0; i < jarr.length(); ++i) {
            JSONObject jobj = jarr.getJSONObject(i);

            // Get the name and array of statuses
            String spellName = jobj.getString(spellNameKey);

            // Load the spell statuses
            boolean fav = jobj.getBoolean(favoriteKey);
            boolean prep = jobj.getBoolean(preparedKey);
            boolean known = jobj.getBoolean(knownKey);
            SpellStatus status = new SpellStatus(fav, prep, known);

            // Add to the map
            spellStatusMap.put(spellName, status);

        }

        // Return the profile
        return new CharacterProfile(charName, spellStatusMap);

    }


}
