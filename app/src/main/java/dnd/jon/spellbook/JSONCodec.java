package dnd.jon.spellbook;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONCodec<T> extends Codec<T, JSONObject, JSONException, JSONException> {};
