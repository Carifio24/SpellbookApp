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

    @FunctionalInterface
    public interface ThrowsJSONFunction<T,R> {
        R apply(T t) throws JSONException;
    }

    static JSONArray loadJSONArrayfromAsset(Context context, String assetFilename) throws JSONException {
        String jsonStr;
        try {
            final InputStream is = context.getAssets().open(assetFilename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonStr = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new JSONArray(jsonStr);
    }

    static JSONObject loadJSONObjectfromAsset(Context context, String assetFilename) throws JSONException {
        String jsonStr;
        try {
            final InputStream is = context.getAssets().open(assetFilename);
            final int size = is.available();
            final byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonStr = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new JSONObject(jsonStr);
    }

    static JSONObject loadJSONfromData(File file) throws JSONException {
        String jsonStr;
        try {
            final FileInputStream is = new FileInputStream(file);
            final int size = is.available();
            final byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonStr = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new JSONObject(jsonStr);
    }

    static String loadAssetAsString(File file) {
        try {
            InputStream is = new FileInputStream(file);
            final int size = is.available();
            final byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    static JSONObject loadJSONfromData(Context context, String dataFilename) throws JSONException {
        return loadJSONfromData(new File(context.getFilesDir(), dataFilename));
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

    static <T> boolean saveAsJSON(T item, JSONUtils.ThrowsJSONFunction<T,JSONObject> jsonifier, File file) {
        try {
            final JSONObject json = jsonifier.apply(item);
            return saveJSON(json, file);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }


}
