package dnd.jon.spellbook;

import android.app.Application;
import android.os.FileObserver;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CharacterProfileViewModel extends ViewModel {

    private static final String PROFILES_DIR_NAME = "Characters";
    private static final String CHARACTER_EXTENSION = ".json";
    private static final List<Character> illegalCharacters = new ArrayList<>(Arrays.asList('\\', '/', '.'));
    private static final String LOGGING_TAG = "character_profile_view_model";

    private final Application application;
    private final File profilesDir;
    private final MutableLiveData<List<String>> characterNamesLD;
    private final MutableLiveData<CharacterProfile> currentProfileLD;

    public CharacterProfileViewModel(Application application) {
        this.application = application;
        this.profilesDir = FilesystemUtils.createFileDirectory(application, PROFILES_DIR_NAME);
        this.currentProfileLD = new MutableLiveData<>();
        this.characterNamesLD = new MutableLiveData<>();
        updateCharacterNames();

        // Whenever a file is created or deleted in the profiles folder
        // we update the list of character names
        final FileObserver observer = new FileObserver(profilesDir) {
            @Override
            public void onEvent(int i, @Nullable String s) {
                switch (i) {
                    case FileObserver.CREATE:
                    case FileObserver.DELETE:
                        updateCharacterNames();
                }
            }
        };
        observer.startWatching();
    }

    String characterNameValidator(String name) {
        if (name.isEmpty()) {
            return application.getString(R.string.empty_name);
        }

        final String nameString = application.getString(R.string.name_lowercase);
        for (Character c : illegalCharacters) {
            final String cStr = c.toString();
            if (name.contains(cStr)) {
                return application.getString(R.string.illegal_character, nameString, cStr);
            }
        }

        final List<String> existingCharacters = characterNamesLD.getValue();
        if (existingCharacters != null && existingCharacters.contains(name)) {
            return application.getString(R.string.duplicate_name);
        }

        return "";
    }

    static boolean isLegal(Character c) {
        return illegalCharacters.contains(c);
    }

    static boolean isLegal(String name) {
        for (int i = 0; i < name.length(); i++) {
            if (!isLegal(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private void updateCharacterNames() {
        final List<String> characterNames = new ArrayList<>();
        final int toRemove = CHARACTER_EXTENSION.length();
        final File[] characterFiles = profilesDir.listFiles();
        if (characterFiles != null) {
            for (File file : characterFiles) {
                final String filename = file.getName();
                if (filename.endsWith(CHARACTER_EXTENSION)) {
                    final String charName = filename.substring(0, filename.length() - toRemove);
                    characterNames.add(charName);
                }
            }
        }
        characterNames.sort(String::compareToIgnoreCase);
        characterNamesLD.setValue(characterNames);
    }

    CharacterProfile getProfileByName(String name) {
        final String characterFile = name + CHARACTER_EXTENSION;
        final File profileLocation = new File(profilesDir, characterFile);
        if (!(profileLocation.exists() && profileLocation.isFile())) {
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

    void setProfile(CharacterProfile profile) {
        currentProfileLD.setValue(profile);
    }

    void setProfileByName(String name) {
        final CharacterProfile profile = getProfileByName(name);
        if (profile != null) {
            setProfile(profile);
        } else {
            // TODO: Toast error message
        }
    }

    boolean saveProfile(CharacterProfile profile) {
        final String filename = profile.getName() + CHARACTER_EXTENSION;
        final File filepath = new File(profilesDir, filename);
        return profile.save(filepath);
    }

    boolean deleteProfile(CharacterProfile profile) {
        return deleteProfileByName(profile.getName());
    }

    boolean deleteProfileByName(String name) {
        final String charFile = name + CHARACTER_EXTENSION;
        final File profileLocation = new File(profilesDir, charFile);
        final boolean success = profileLocation.delete();

        if (!success) {
            Log.v(LOGGING_TAG, "Error deleting character: " + profileLocation);
        }
//        else {
//            System.out.println("Successfully deleted the data file for " + name);
//            System.out.println("File location was " + profileLocationStr);
//        }

        final CharacterProfile currentProfile = currentProfileLD.getValue();
        if (success && currentProfile != null && name.equals(currentProfile.getName())) {
            final List<String> characters = characterNamesLD.getValue();
            if (characters != null && characters.size() > 0) {
                setProfileByName(characters.get(0));
            } else {
                currentProfileLD.setValue(null);
            }
        }

        return success;
    }

    void update() {
        updateCharacterNames();
    }

    LiveData<CharacterProfile> getCurrentProfile() { return currentProfileLD; }

    LiveData<List<String>> getCharacterNames() { return characterNamesLD; }

}
