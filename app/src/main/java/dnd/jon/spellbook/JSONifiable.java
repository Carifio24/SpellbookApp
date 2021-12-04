package dnd.jon.spellbook;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONifiable {
    JSONObject toJSON() throws JSONException;
}
