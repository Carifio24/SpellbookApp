package dnd.jon.spellbook;

import android.app.Application;
import android.os.FileObserver;
import android.util.Log;
import android.util.Pair;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.databinding.Observable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpellbookViewModel extends ViewModel implements Filterable {

    private static final String PROFILES_DIR_NAME = "Characters";
    private static final String CHARACTER_EXTENSION = ".json";
    private static final List<Character> ILLEGAL_CHARACTERS = new ArrayList<>(Arrays.asList('\\', '/', '.'));
    private static final String LOGGING_TAG = "spellbook_view_model";

    private final Application application;

    private final File profilesDir;
    private final MutableLiveData<List<String>> characterNamesLD;
    private CharacterProfile profile = null;
    private CharSequence searchQuery;
    private boolean filterNeeded;
    private boolean spellTableVisible;
    private final MutableLiveData<CharacterProfile> currentProfileLD;
    private final LiveData<SpellFilterStatus> currentSpellFilterStatusLD;
    private final LiveData<SortFilterStatus> currentSortFilterStatusLD;
    private final LiveData<SpellSlotStatus> currentSpellSlotStatusLD;

    private static List<Spell> englishSpells = new ArrayList<>();
    private final List<Spell> spells;
    private final MutableLiveData<List<Spell>> currentSpellsLD;
    private final MutableLiveData<Spell> currentSpellLD;
    private static final String englishSpellsFilename = "Spells.json";

    private static final List<Integer> SORT_PROPERTY_IDS = Arrays.asList(BR.firstSortField, BR.firstSortReverse, BR.secondSortField, BR.secondSortReverse);

    private static <S,T> LiveData<T> distinctTransform(LiveData<S> source, Function<S,T> transform) {
        return Transformations.distinctUntilChanged(Transformations.map(source, transform));
    }

    public SpellbookViewModel(Application application) {
        this.application = application;
        this.profilesDir = FilesystemUtils.createFileDirectory(application, PROFILES_DIR_NAME);
        this.currentProfileLD = new MutableLiveData<>();
        this.characterNamesLD = new MutableLiveData<>();
        this.currentSpellFilterStatusLD = distinctTransform(currentProfileLD, CharacterProfile::getSpellFilterStatus);
        this.currentSortFilterStatusLD = distinctTransform(currentProfileLD, CharacterProfile::getSortFilterStatus);
        this.currentSpellSlotStatusLD = distinctTransform(currentProfileLD, CharacterProfile::getSpellSlotStatus);
        final String spellsFilename = application.getResources().getString(R.string.spells_filename);
        this.spells = loadSpellsFromFile(spellsFilename, false);
        this.currentSpellsLD = new MutableLiveData<>(spells);
        this.currentSpellLD = new MutableLiveData<>();
        this.filterNeeded = false;
        this.spellTableVisible = true;
        updateCharacterNames();

        // If we don't already have the english spells, get them
        if (englishSpells.size() == 0) {
            englishSpells = loadSpellsFromFile(englishSpellsFilename, true);
        }

        // Whenever a file is created or deleted in the profiles folder
        // we update the list of character names
        final FileObserver observer = new FileObserver(profilesDir) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                switch (event) {
                    case FileObserver.CREATE:
                    case FileObserver.DELETE:
                        updateCharacterNames();
                }
            }
        };
        observer.startWatching();
    }

    private List<Spell> loadSpellsFromFile(String filename, boolean useInternalParse) {
        try {
            final JSONArray jsonArray = JSONUtils.loadJSONArrayfromAsset(application, filename);
            final SpellCodec codec = new SpellCodec(application);
            return codec.parseSpellList(jsonArray, useInternalParse);
        } catch (Exception e) {
            //TODO: Better error handling?
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    LiveData<Spell> getCurrentSpell() { return currentSpellLD; }

    void setCurrentSpell(Spell spell) {
        currentSpellLD.setValue(spell);
    }

    List<Spell> getAllSpells() { return spells; }

    String characterNameValidator(String name) {
        if (name.isEmpty()) {
            return application.getString(R.string.empty_name);
        }

        final String nameString = application.getString(R.string.name_lowercase);
        for (Character c : ILLEGAL_CHARACTERS) {
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
        return !ILLEGAL_CHARACTERS.contains(c);
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
        this.profile = profile;
        currentProfileLD.setValue(profile);

        final SortFilterStatus sortFilterStatus = profile.getSortFilterStatus();
        sortFilterStatus.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (sender != sortFilterStatus) { return; }
                if (SORT_PROPERTY_IDS.contains(propertyId)) {
                    sort();
                } else {
                    setFilterNeeded(true);
                }
            }
        });
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

    SpellFilterStatus getSpellFilterStatus() { return profile.getSpellFilterStatus(); }
    SortFilterStatus getSortFilterStatus() { return profile.getSortFilterStatus(); }
    SpellSlotStatus getSpellSlotStatus() { return profile.getSpellSlotStatus(); }
    LiveData<CharacterProfile> currentProfile() { return currentProfileLD; }
    LiveData<SpellFilterStatus> currentSpellFilterStatus() { return currentSpellFilterStatusLD; }
    LiveData<SortFilterStatus> currentSortFilterStatus() { return currentSortFilterStatusLD; }
    LiveData<SpellSlotStatus> currentSpellSlotStatus() { return currentSpellSlotStatusLD; }

    LiveData<List<String>> getCharacterNames() { return characterNamesLD; }

    boolean saveCurrentProfile() {
        final CharacterProfile profile = currentProfileLD.getValue();
        if (profile != null) {
            return saveProfile(profile);
        }
        return false;
    }

    boolean saveSpellFilterStatus() { return saveCurrentProfile(); }
    boolean saveSortFilterStatus() { return saveCurrentProfile(); }
    boolean saveSpellSlotStatus() { return saveCurrentProfile(); }

    CharSequence getSearchQuery() { return searchQuery; }
    void setSearchQuery(CharSequence searchQuery) {
        this.searchQuery = searchQuery;
        setFilterNeeded(true);
    }

    LiveData<Boolean> getUseExpanded() { return distinctTransform(currentSortFilterStatusLD, SortFilterStatus::getUseTashasExpandedLists); }

    private void updateProperty(TriConsumer<SpellFilterStatus,Spell,Boolean> propertyUpdater, Spell spell, boolean value) {
        final SpellFilterStatus spellFilterStatus = currentSpellFilterStatusLD.getValue();
        if (spellFilterStatus == null) { return; }
        propertyUpdater.accept(spellFilterStatus, spell, value);
    }
    void updateFavorite(Spell spell, boolean favorite) {
        updateProperty(SpellFilterStatus::setFavorite, spell, favorite);
    }
    void updatePrepared(Spell spell, boolean prepared) {
        updateProperty(SpellFilterStatus::setPrepared, spell, prepared);
    }
    void updateKnown(Spell spell, boolean known) {
        updateProperty(SpellFilterStatus::setKnown, spell, known);
    }

    SpellStatus getSpellStatus(Spell spell) {
        final SpellFilterStatus spellFilterStatus = currentSpellFilterStatusLD.getValue();
        if (spellFilterStatus == null) { return null; }
        return spellFilterStatus.getStatus(spell);
    }

    void setFilteredSpells(List<Spell> filteredSpells) {
        this.currentSpellsLD.setValue(filteredSpells);
        setFilterNeeded(false);
    }

    void sort() {
        final List<Spell> currentSpells = currentSpellsLD.getValue();
        if (currentSpells == null) { return; }
        final SortFilterStatus sortFilterStatus = profile.getSortFilterStatus();
        final List<Pair<SortField,Boolean>> sortParameters = new ArrayList<Pair<SortField,Boolean>>() {{
            add(new Pair<>(sortFilterStatus.getFirstSortField(), sortFilterStatus.getFirstSortReverse()));
            add(new Pair<>(sortFilterStatus.getSecondSortField(), sortFilterStatus.getSecondSortReverse()));
        }};
        currentSpells.sort(new SpellComparator(application, sortParameters));
        currentSpellsLD.setValue(currentSpells);
    }

    LiveData<List<Spell>> getCurrentSpells() { return currentSpellsLD; }

    @Override
    public Filter getFilter() {
        return new SpellFilter(this);
    }

    private void filter() {
        getFilter().filter(searchQuery);
    }

    private void filterIfAppropriate() {
        if (filterNeeded && spellTableVisible) {
            filter();
        }
    }

    void setFilterNeeded(boolean filterNeeded) {
        this.filterNeeded = filterNeeded;
        filterIfAppropriate();
    }

    void setSpellTableVisible(boolean visible) {
        this.spellTableVisible = visible;
        filterIfAppropriate();
    }

    static List<Spell> allEnglishSpells() { return englishSpells; }
}
