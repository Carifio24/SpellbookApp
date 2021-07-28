package dnd.jon.spellbook;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class CharacterProfileUtils {

    private static final String profilesDirName = "Characters";
    private static final String CHARACTER_EXTENSION = ".json";
    private static final String LOGGING_TAG = "CharacterProfileUtils";

    static final ArrayList<Character> illegalCharacters = new ArrayList<>(Arrays.asList('\\', '/', '.'));

    static String characterNameValidator(Context context, String name, List<String> existingCharacters) {

        if (name.isEmpty()) {
            return context.getString(R.string.empty_name);
        }

        final String nameString = context.getString(R.string.name_lowercase);
        for (Character c : illegalCharacters) {
            final String cStr = c.toString();
            if (name.contains(cStr)) {
                return context.getString(R.string.illegal_character, nameString, cStr);
            }
        }

        if (existingCharacters.contains(name)) {
            return context.getString(R.string.duplicate_name);
        }

        return "";

    }

    // Returns the current list of characters
    static ArrayList<String> charactersList(Context context) {
        final File profilesDir = FilesystemUtils.createFileDirectory(context, profilesDirName);
        final ArrayList<String> charList = new ArrayList<>();
        final int toRemove = CHARACTER_EXTENSION.length();
        final File[] characterFiles = profilesDir.listFiles();
        if (characterFiles != null) {
            for (File file : characterFiles) {
                final String filename = file.getName();
                if (filename.endsWith(CHARACTER_EXTENSION)) {
                    final String charName = filename.substring(0, filename.length() - toRemove);
                    charList.add(charName);
                }
            }
        }
        charList.sort(String::compareToIgnoreCase);
        return charList;
    }

    static CharacterProfile getProfileByName(Context context, String name) {

        final File profilesDir = FilesystemUtils.createFileDirectory(context, profilesDirName);
        final String characterFile = name + CHARACTER_EXTENSION;
        final File profileLocation = new File(profilesDir, characterFile);
        if (! (profileLocation.exists() && profileLocation.isFile()) ) {
            return null;
        }

        try {
            final JSONObject json = JSONUtils.loadJSONfromData(profileLocation);
            if (json == null) {
                return null;
            }
            return CharacterProfile.fromJSON(json);
        } catch (JSONException e) {
            final String charStr = JSONUtils.loadAssetAsString(profileLocation);
            Log.v(LOGGING_TAG, "The offending JSON is: " + charStr);
            return null;
        }

    }
}
