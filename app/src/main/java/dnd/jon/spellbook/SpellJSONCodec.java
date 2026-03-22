package dnd.jon.spellbook;

import android.content.Context;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public interface SpellJSONCodec {
    Spell parseSpell(JSONObject json, SpellBuilder builder) throws JSONException;
    List<Spell> parseSpellList(@Nullable JSONArray jsonArray, Locale locale) throws Exception;
    JSONObject toJSON(Spell spell, Context context);
    JSONObject toJSON(Spell spell);
}
