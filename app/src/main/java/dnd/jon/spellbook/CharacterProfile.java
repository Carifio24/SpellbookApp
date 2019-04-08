package dnd.jon.spellbook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class CharacterProfile {

    // The map of spell statuses
    String name;
    HashMap<String, SpellStatus> spellStatuses;

    // Keys for loading/saving
    static private String charNameKey = "CharacterName";
    static private String spellsKey = "Spells";
    static private String spellNameKey = "SpellName";
    static private String favoriteKey = "Favorite";
    static private String preparedKey = "Prepared";
    static private String knownKey = "Known";


    CharacterProfile(String nameIn, HashMap<String, SpellStatus> spellStatusesIn) {
        name = nameIn;
        spellStatuses = spellStatusesIn;
    }

    // Save to JSON
    JSONObject toJSON() throws JSONException {

        // The JSON object
        JSONObject json = new JSONObject();

        // Store the data
        json.put(charNameKey, name);
        JSONArray spellStatusJA = new JSONArray();
        for (HashMap.Entry<String, SpellStatus> data : spellStatuses.entrySet()) {
            JSONObject statusJSON = new JSONObject();
            statusJSON.put(spellNameKey, data.getKey());
            SpellStatus status = data.getValue();
            statusJSON.put(favoriteKey, status.favorite);
            statusJSON.put(preparedKey, status.prepared);
            statusJSON.put(knownKey, status.known);
            spellStatusJA.put(statusJSON);
        }
        json.put(spellsKey, spellStatusJA);

        return json;
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
            SpellStatus status = new SpellStatus();
            status.favorite = jobj.getBoolean(favoriteKey);
            status.prepared = jobj.getBoolean(preparedKey);
            status.known = jobj.getBoolean(knownKey);

            // Add to the map
            spellStatusMap.put(spellName, status);

        }

        // Return the profile
        return new CharacterProfile(charName, spellStatusMap);

    }


}
