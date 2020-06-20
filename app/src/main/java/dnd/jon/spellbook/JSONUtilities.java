package dnd.jon.spellbook;

import android.content.Context;

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

public class JSONUtilities {

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

    JSONObject loadJSONfromData(Context context, String dataFilename) throws JSONException {
        return loadJSONfromData(new File(context.getApplicationContext().getFilesDir(), dataFilename));
    }

    private void saveJSON(JSONObject json, File file) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveJSON(JSONArray json, File file) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(json.toString(4));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
