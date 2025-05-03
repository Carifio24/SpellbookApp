package dnd.jon.spellbook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public interface SpellCodec {
    Spell parseSpell(JSONObject json, SpellBuilder builder) throws JSONException;
    List<Spell> parseSpellList(JSONArray jsonArray);
    JSONObject toJSON(Spell spell) throws JSONException;
}
