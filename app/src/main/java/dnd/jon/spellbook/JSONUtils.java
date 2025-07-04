package dnd.jon.spellbook;

import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;

class JSONUtils {

    static private final String SOURCE_NAME_KEY = "name";
    static private final String SOURCE_CODE_KEY = "code";
    static private final String SOURCE_SPELLS_KEY = "spells";

//    private static <T> T loadItemFromInputStream(InputStream inputStream, ThrowsExceptionFunction<String,T,IOException> creator) {
//        try {
//            final String str = stringFromInputStream(inputStream);
//            return creator.apply(str);
//        } catch (IOException e) {
//            return null;
//        }
//    }

    private static <T> T loadJSONFromAsset(Context context, String assetFilename, SpellbookUtils.ThrowsExceptionFunction<String,T,JSONException> creator) throws JSONException {
        try {
            final InputStream inputStream = context.getAssets().open(assetFilename);
            final String str = AndroidUtils.stringFromInputStream(inputStream);
            return creator.apply(str);
        } catch (IOException e) {
            return null;
        }
    }

    static JSONArray loadJSONArrayFromAsset(Context context, String assetFilename) throws JSONException {
        return loadJSONFromAsset(context, assetFilename, JSONArray::new);
    }

    static JSONObject loadJSONObjectFromAsset(Context context, String assetFilename) throws JSONException {
        return loadJSONFromAsset(context, assetFilename, JSONObject::new);
    }

    static JSONObject loadJSONFromData(File file) throws JSONException {
        try {
            final InputStream inputStream = new FileInputStream(file);
            final String str = AndroidUtils.stringFromInputStream(inputStream);
            return new JSONObject(str);
        } catch (IOException e) {
            return null;
        }
    }

    static String loadAssetAsString(File file) {
        try {
            final InputStream inputStream = new FileInputStream(file);
            return AndroidUtils.stringFromInputStream(inputStream);
        } catch (IOException e) {
            return null;
        }
    }

    static JSONObject loadJSONFromData(Context context, String dataFilename) throws JSONException {
        return loadJSONFromData(new File(context.getFilesDir(), dataFilename));
    }

    static <T> T loadItemFromJSONData(File file, SpellbookUtils.ThrowsExceptionFunction<JSONObject,T,JSONException> createFromJSON) throws JSONException {
        final JSONObject json = loadJSONFromData(file);
        return createFromJSON.apply(json);
    }

    private static boolean saveJSON(JSONObject json, File file) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(json.toString(4));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean saveJSON(JSONArray json, File file) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(json.toString(4));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static <T extends JSONifiable> boolean saveAsJSON(T item, File file) {
        return saveAsJSON(item, T::toJSON, file);
    }

    static <T> boolean saveAsJSON(T item, SpellbookUtils.ThrowsExceptionFunction<T,JSONObject,JSONException> jsonifier, File file) {
        try {
            final JSONObject json = jsonifier.apply(item);
            return saveJSON(json, file);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    // TODO: Think about a better way to do this
    // The following methods should only be used for created sources
    static JSONObject asJSON(Source source, Context context, Collection<Spell> spells) throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(SOURCE_NAME_KEY, DisplayUtils.getDisplayName(source, context));
        json.put(SOURCE_CODE_KEY, DisplayUtils.getCode(source, context));
        if (spells != null) {
            final JSONArray spellArray = new JSONArray();
            final SpellCodec codec = new SpellCodec(context);
            for (Spell spell : spells) {
                spellArray.put(codec.toJSON(spell));
            }
            json.put(SOURCE_SPELLS_KEY, spellArray);
        }
        return json;
    }

    static JSONObject asJSON(Source source, Context context) throws JSONException {
        return asJSON(source, context, null);
    }

    static Source sourceFromJSON(JSONObject json) throws JSONException {
        final String name = json.getString(SOURCE_NAME_KEY);
        final String code = json.getString(SOURCE_CODE_KEY);
        return Source.create(name, code);
    }

    static Pair<Source, List<Spell>> sourceWithSpellsFromJSON(JSONObject json, Context context) throws JSONException {
        final Source source = sourceFromJSON(json);
        final JSONArray jsonSpells = json.optJSONArray(SOURCE_SPELLS_KEY);
        final SpellCodec codec = new SpellCodec(context);
        List<Spell> spells = null;
        if (jsonSpells != null) {
            spells = new ArrayList<>();
            final SpellBuilder builder = new SpellBuilder(context);
            for (int i = 0; i < jsonSpells.length(); i++) {
                final JSONObject item = jsonSpells.getJSONObject(i);
                final Spell spell = codec.parseSpell(item, builder, true);
                if (spell != null) {
                    spells.add(spell);
                }
            }
        }
        return new Pair<>(source, spells);
    }

    static Source sourceFromJSON(File file) throws JSONException {
        return loadItemFromJSONData(file, JSONUtils::sourceFromJSON);
    }

    static Pair<Source, List<Spell>> sourceWithSpellsFromJSON(File file, Context context) throws JSONException {
        return loadItemFromJSONData(file, (object) -> JSONUtils.sourceWithSpellsFromJSON(object, context));
    }

}
