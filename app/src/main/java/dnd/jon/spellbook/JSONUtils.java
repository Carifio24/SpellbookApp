package dnd.jon.spellbook;

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
import java.util.function.Function;

import android.content.Context;

class JSONUtils {

    static private final String SOURCE_NAME_KEY = "name";
    static private final String SOURCE_CODE_KEY = "code";

    private static String stringFromInputStream(InputStream inputStream) throws IOException {
        final int size = inputStream.available();
        final byte[] buffer = new byte[size];
        inputStream.read(buffer);
        inputStream.close();
        return new String(buffer, StandardCharsets.UTF_8);
    }

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
            final String str = stringFromInputStream(inputStream);
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
            final String str = stringFromInputStream(inputStream);
            return new JSONObject(str);
        } catch (IOException e) {
            return null;
        }
    }

    static String loadAssetAsString(File file) {
        try {
            final InputStream inputStream = new FileInputStream(file);
            return stringFromInputStream(inputStream);
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
    // The following two methods should only be used for created sources
    static JSONObject asJSON(Source source, Context context) throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(SOURCE_NAME_KEY, DisplayUtils.getDisplayName(source, context));
        json.put(SOURCE_CODE_KEY, DisplayUtils.getCode(source, context));
        return json;
    }

    static Source sourceFromJSON(JSONObject json) throws JSONException {
        final String name = json.getString(SOURCE_NAME_KEY);
        final String code = json.getString(SOURCE_CODE_KEY);
        return new Source(name, code);
    }

    static Source sourceFromJSON(File file) throws JSONException {
        return loadItemFromJSONData(file, JSONUtils::sourceFromJSON);
    }

}
