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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class SpellbookViewModel extends ViewModel implements Filterable {

    private static final String PROFILES_DIR_NAME = "Characters";
    private static final String STATUSES_DIR_NAME = "SortFilterStatus";
    private static final String JSON_EXTENSION = ".json";
    private static final String CHARACTER_EXTENSION = JSON_EXTENSION;
    private static final String STATUS_EXTENSION = JSON_EXTENSION;
    private static final List<Character> ILLEGAL_CHARACTERS = new ArrayList<>(Arrays.asList('\\', '/', '.'));
    private static final String LOGGING_TAG = "spellbook_view_model";
    private static final String ENGLISH_SPELLS_FILENAME = "Spells.json";
    private static final String SETTINGS_FILE = "Settings.json";

    private final Application application;

    private final Settings settings;

    private final File profilesDir;
    private final File statusesDir;
    private final FileObserver profilesDirObserver;
    private final FileObserver statusesDirObserver;

    private final MutableLiveData<List<String>> characterNamesLD;
    private final MutableLiveData<List<String>> statusNamesLD;
    private CharacterProfile profile = null;
    private CharSequence searchQuery;
    private boolean filterNeeded = false;
    private boolean sortNeeded = false;
    private boolean spellTableVisible = true;
    private boolean suspendSpellListModifications = false;
    private final MutableLiveData<CharacterProfile> currentProfileLD;
    private final MutableLiveData<SpellFilterStatus> currentSpellFilterStatusLD;
    private final MutableLiveData<SortFilterStatus> currentSortFilterStatusLD;
    private final MutableLiveData<SpellSlotStatus> currentSpellSlotStatusLD;

    private static List<Spell> englishSpells = new ArrayList<>();
    private final List<Spell> spells;
    private final MutableLiveData<List<Spell>> currentSpellsLD;
    private final MutableLiveData<Boolean> currentSpellFavoriteLD;
    private final MutableLiveData<Boolean> currentSpellPreparedLD;
    private final MutableLiveData<Boolean> currentSpellKnownLD;
    private final MutableLiveData<Spell> currentSpellLD;
    private final LiveData<Boolean> currentUseExpandedLD;

    private static final List<Integer> SORT_PROPERTY_IDS = Arrays.asList(BR.firstSortField, BR.firstSortReverse, BR.secondSortField, BR.secondSortReverse);

    private static <S,T> LiveData<T> distinctTransform(LiveData<S> source, Function<S,T> transform) {
        return Transformations.distinctUntilChanged(Transformations.map(source, transform));
    }

    public SpellbookViewModel(Application application) {
        this.application = application;

        this.profilesDir = FilesystemUtils.createFileDirectory(application, PROFILES_DIR_NAME);
        this.statusesDir = FilesystemUtils.createFileDirectory(application, STATUSES_DIR_NAME);

        this.currentProfileLD = new MutableLiveData<>();
        this.characterNamesLD = new MutableLiveData<>();
        this.statusNamesLD = new MutableLiveData<>();
        this.currentSpellFilterStatusLD = new MutableLiveData<>();
        this.currentSortFilterStatusLD = new MutableLiveData<>();
        this.currentSpellSlotStatusLD = new MutableLiveData<>();
        final String spellsFilename = application.getResources().getString(R.string.spells_filename);
        this.spells = loadSpellsFromFile(spellsFilename, false);
        this.currentSpellsLD = new MutableLiveData<>(spells);
        this.currentSpellLD = new MutableLiveData<>();
        this.currentSpellFavoriteLD = new MutableLiveData<>();
        this.currentSpellPreparedLD = new MutableLiveData<>();
        this.currentSpellKnownLD = new MutableLiveData<>();
        this.currentUseExpandedLD = distinctTransform(currentSortFilterStatusLD, SortFilterStatus::getUseTashasExpandedLists);
        updateCharacterNames();

        // Load the settings and the character profile
        this.settings = loadSettings();
        final String charName = settings.characterName();
        if (charName != null) {
            setProfileByName(charName);
        }

        // If we don't already have the english spells, get them
        if (englishSpells.size() == 0) {
            englishSpells = loadSpellsFromFile(ENGLISH_SPELLS_FILENAME, true);
        }

        // Whenever a file is created or deleted in the profiles folder
        // we update the list of character names
        this.profilesDirObserver = filenamesObserver(profilesDir, this::updateCharacterNames);
        profilesDirObserver.startWatching();

        // Same with the sort/filter statuses
        this.statusesDirObserver = filenamesObserver(statusesDir, this::updateStatusNames);
        statusesDirObserver.startWatching();
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

    private FileObserver filenamesObserver(File directory, Runnable executeOnEvent) {
        return new FileObserver(directory) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                switch (event) {
                    case FileObserver.CREATE:
                    case FileObserver.DELETE:
                        executeOnEvent.run();
                }
            }
        };
    }

    LiveData<Spell> currentSpell() { return currentSpellLD; }
    LiveData<Boolean> currentSpellFavoriteLD() { return currentSpellFavoriteLD; }
    LiveData<Boolean> currentSpellPreparedLD() { return currentSpellPreparedLD; }
    LiveData<Boolean> currentSpellKnownLD() { return currentSpellKnownLD; }

    void setCurrentSpell(Spell spell) {
        currentSpellLD.setValue(spell);
        currentSpellFavoriteLD.setValue(getFavorite(spell));
        currentSpellPreparedLD.setValue(getPrepared(spell));
        currentSpellKnownLD.setValue(getKnown(spell));
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

    static Character firstIllegalCharacter(String name) {
        for (int i = 0; i < name.length(); i++) {
            final Character c = name.charAt(i);
            if (!isLegal(c)) {
                return c;
            }
        }
        return null;
    }

    private void updateNamesFromDirectory(File directory, String extension, MutableLiveData<List<String>> liveData) {
        final List<String> names = new ArrayList<>();
        final int toRemove = extension.length();
        final File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                final String filename = file.getName();
                if (filename.endsWith(extension)) {
                    final String name = filename.substring(0, filename.length() - toRemove);
                    names.add(name);
                }
            }
        }
        names.sort(String::compareToIgnoreCase);
        liveData.postValue(names);
    }

    private void updateCharacterNames() {
        updateNamesFromDirectory(profilesDir, CHARACTER_EXTENSION, characterNamesLD);
    }

    private void updateStatusNames() {
        updateNamesFromDirectory(statusesDir, STATUS_EXTENSION, statusNamesLD);
    }

    private <T> T getDataItemByName(String name, String extension, File directory, JSONUtils.ThrowsJSONFunction<JSONObject,T> creator) {
        final String filename = name + extension;
        final File filepath = new File(directory, filename);
        if (!(filepath.exists() && filepath.isFile())) {
            return null;
        }

        try {
            final JSONObject json = JSONUtils.loadJSONfromData(filepath);
            if (json == null) { return null; }
            return creator.apply(json);
        } catch (JSONException e) {
            final String str = JSONUtils.loadAssetAsString(filepath);
            Log.v(LOGGING_TAG, "The offending JSON is: " + str);
            return null;
        }
    }

    CharacterProfile getProfileByName(String name) {
        return getDataItemByName(name, CHARACTER_EXTENSION, profilesDir, CharacterProfile::fromJSON);
    }

    SortFilterStatus getSortFilterStatusByName(String name) {
        return getDataItemByName(name, STATUS_EXTENSION, statusesDir, SortFilterStatus::fromJSON);
    }

    CharacterProfile getProfile() { return profile; }

    void setProfile(CharacterProfile profile) {
        this.profile = profile;
        currentProfileLD.setValue(profile);
        currentSortFilterStatusLD.setValue(profile.getSortFilterStatus());
        currentSpellFilterStatusLD.setValue(profile.getSpellFilterStatus());
        currentSpellSlotStatusLD.setValue(profile.getSpellSlotStatus());
        settings.setCharacterName(profile.getName());
        setupSortFilterObserver();

        profile.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (sender != profile) { return; }
                if (propertyId == BR.sortFilterStatus) {
                    setupSortFilterObserver();
                }
            }
        });
    }

    private void setupSortFilterObserver() {
        final SortFilterStatus sortFilterStatus = profile.getSortFilterStatus();
        sortFilterStatus.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (sender != sortFilterStatus) { return; }
                if (SORT_PROPERTY_IDS.contains(propertyId)) {
                    setSortNeeded(true);
                } else {
                    setFilterNeeded(true);
                }
                // Let's try this
                saveCurrentProfile();
            }
        });
        setSortNeeded(true);
        setFilterNeeded(true);
    }

    void setProfileByName(String name) {
        final CharacterProfile profile = getProfileByName(name);
        if (profile != null) {
            setProfile(profile);
        } else {
            // TODO: Toast error message
        }
    }

    void setSortFilterStatus(SortFilterStatus sortFilterStatus) {
        setSuspendSpellListModifications(true);
        profile.setSortFilterStatus(sortFilterStatus);
        this.currentSortFilterStatusLD.setValue(sortFilterStatus);
        setSuspendSpellListModifications(false);
    }

    void setSortFilterStatusByName(String name) {
        final SortFilterStatus status = getSortFilterStatusByName(name);
        if (status != null) {
            setSortFilterStatus(status);
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

    private boolean deleteItemByName(String name, String extension, File directory) {
        final String filename = name + extension;
            final File filepath = new File(directory, filename);
        final boolean success = filepath.delete();
        if (!success) {
            Log.v(LOGGING_TAG, "Error deleting item: " + filepath);
        }
        return success;
    }

    boolean deleteProfileByName(String name) {
        final boolean success = deleteItemByName(name, CHARACTER_EXTENSION, profilesDir);
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

    boolean deleteSortFilterStatusByName(String name) {
        return deleteItemByName(name, STATUS_EXTENSION, statusesDir);
    }

    void update() {
        updateCharacterNames();
    }

    SpellFilterStatus getSpellFilterStatus() { return profile != null ? profile.getSpellFilterStatus() : null; }
    SortFilterStatus getSortFilterStatus() { return profile != null ? profile.getSortFilterStatus() : null; }
    SpellSlotStatus getSpellSlotStatus() { return profile != null ? profile.getSpellSlotStatus() : null; }
    LiveData<CharacterProfile> currentProfile() { return currentProfileLD; }
    LiveData<SpellFilterStatus> currentSpellFilterStatus() { return currentSpellFilterStatusLD; }
    LiveData<SortFilterStatus> currentSortFilterStatus() { return currentSortFilterStatusLD; }
    LiveData<SpellSlotStatus> currentSpellSlotStatus() { return currentSpellSlotStatusLD; }

    LiveData<List<String>> currentCharacterNames() { return characterNamesLD; }
    LiveData<List<String>> currentStatusNames() { return statusNamesLD; }

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

    LiveData<Boolean> currentUseExpanded() {
        return currentUseExpandedLD;
    }

    private void setProperty(TriConsumer<SpellFilterStatus,Spell,Boolean> propertyUpdater,
                             Spell spell, boolean value, MutableLiveData<Boolean> liveData) {
        final SpellFilterStatus spellFilterStatus = profile.getSpellFilterStatus();
        if (spellFilterStatus == null) { return; }
        propertyUpdater.accept(spellFilterStatus, spell, value);
        liveData.setValue(value);
    }
    void setFavorite(Spell spell, boolean favorite) {
        setProperty(SpellFilterStatus::setFavorite, spell, favorite, currentSpellFavoriteLD);
    }
    void setPrepared(Spell spell, boolean prepared) {
        setProperty(SpellFilterStatus::setPrepared, spell, prepared, currentSpellPreparedLD);
    }
    void setKnown(Spell spell, boolean known) {
        setProperty(SpellFilterStatus::setKnown, spell, known, currentSpellKnownLD);
    }

    private Boolean getProperty(BiFunction<SpellFilterStatus,Spell,Boolean> getter, Spell spell) {
        final SpellFilterStatus spellFilterStatus = profile.getSpellFilterStatus();
        return getter.apply(spellFilterStatus, spell);
    }

    Boolean getFavorite(Spell spell) { return getProperty(SpellFilterStatus::isFavorite, spell); }
    Boolean getPrepared(Spell spell) { return getProperty(SpellFilterStatus::isPrepared, spell); }
    Boolean getKnown(Spell spell) { return getProperty(SpellFilterStatus::isKnown, spell); }

    private void toggleProperty(Spell spell, Function<Spell,Boolean> getter, BiConsumer<Spell,Boolean> setter) {
        setter.accept(spell, !getter.apply(spell));
    }

    void toggleFavorite(Spell spell) { toggleProperty(spell, this::getFavorite, this::setFavorite); }
    void togglePrepared(Spell spell) { toggleProperty(spell, this::getPrepared, this::setPrepared); }
    void toggleKnown(Spell spell) { toggleProperty(spell, this::getKnown, this::setKnown); }

    SpellStatus getSpellStatus(Spell spell) {
        final SpellFilterStatus spellFilterStatus = profile.getSpellFilterStatus();
        if (spellFilterStatus != null) {
            return spellFilterStatus.getStatus(spell);
        }
        return null;
    }

    void setFilteredSpells(List<Spell> filteredSpells) {
        this.currentSpellsLD.setValue(filteredSpells);
        setFilterNeeded(false);
    }

    int getIndex(Spell spell) {
        final List<Spell> currentSpells = currentSpellsLD.getValue();
        if (currentSpells == null) { return -1; }
        return currentSpells.indexOf(spell);
    }

    private void sort() {
        final List<Spell> currentSpells = currentSpellsLD.getValue();
        if (currentSpells == null) { return; }
        final SortFilterStatus sortFilterStatus = profile.getSortFilterStatus();
        final List<Pair<SortField,Boolean>> sortParameters = new ArrayList<Pair<SortField,Boolean>>() {{
            add(new Pair<>(sortFilterStatus.getFirstSortField(), sortFilterStatus.getFirstSortReverse()));
            add(new Pair<>(sortFilterStatus.getSecondSortField(), sortFilterStatus.getSecondSortReverse()));
        }};
        currentSpells.sort(new SpellComparator(application, sortParameters));
        currentSpellsLD.setValue(currentSpells);
        setSortNeeded(false);
    }

    LiveData<List<Spell>> currentSpells() { return currentSpellsLD; }

    @Override
    public Filter getFilter() {
        return new SpellFilter(this);
    }

    private void filter() {
        getFilter().filter(searchQuery);
    }

    void setSuspendSpellListModifications(boolean suspendSpellListModifications) {
        this.suspendSpellListModifications = suspendSpellListModifications;
        if (!suspendSpellListModifications) {
            modifySpellsIfAppropriate();
        }
    }

    private void modifySpellsIfAppropriate() {
        if (!suspendSpellListModifications && spellTableVisible) {
            if (filterNeeded) { filter(); }
            if (sortNeeded) { sort(); }
        }
    }

    void setFilterNeeded(boolean filterNeeded) {
        this.filterNeeded = filterNeeded;
        modifySpellsIfAppropriate();
    }

    void setSortNeeded(boolean sortNeeded) {
        this.sortNeeded = sortNeeded;
        modifySpellsIfAppropriate();
    }

    void setSpellTableVisible(boolean visible) {
        this.spellTableVisible = visible;
        modifySpellsIfAppropriate();
    }

    private Settings loadSettings() {
        // Load the settings and the character profile
        try {
            final JSONObject json = JSONUtils.loadJSONfromData(application, SETTINGS_FILE);
            return new Settings(json);
        } catch (Exception e) {
            String s = JSONUtils.loadAssetAsString(new File(SETTINGS_FILE));
            Log.v(LOGGING_TAG, "Error loading settings");
            Log.v(LOGGING_TAG, "The settings file content is: " + s);
            final Settings settings = new Settings();
            final List<String> characterList = characterNamesLD.getValue();
            if (characterList != null && characterList.size() > 0) {
                final String firstCharacter = characterList.get(0);
                settings.setCharacterName(firstCharacter);
            }
            e.printStackTrace();
            return settings;
        }
    }

    // Saves the current settings to a file, in JSON format
    boolean saveSettings() {
        final File settingsLocation = new File(application.getFilesDir(), SETTINGS_FILE);
        return settings.save(settingsLocation);
    }

    static List<Spell> allEnglishSpells() { return englishSpells; }
}
