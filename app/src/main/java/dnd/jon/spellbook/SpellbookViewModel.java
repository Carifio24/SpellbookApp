package dnd.jon.spellbook;

import android.app.Application;
import android.content.Context;
import android.os.FileObserver;
import android.util.Log;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

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
import java.util.function.Consumer;

public class SpellbookViewModel extends ViewModel implements Filterable {

    private static final String PROFILES_DIR_NAME = "Characters";
    private static final String STATUSES_DIR_NAME = "SortFilterStatus";
    private static final String CREATED_SOURCES_DIR_NAME = "Sources";
    private static final String CREATED_SPELLS_DIR_NAME = "CreatedSpells";
    private static final String JSON_EXTENSION = ".json";
    static final String CHARACTER_EXTENSION = JSON_EXTENSION;
    static final String STATUS_EXTENSION = JSON_EXTENSION;
    static final String CREATED_SOURCE_EXTENSION = JSON_EXTENSION;
    static final String CREATED_SPELL_EXTENSION = JSON_EXTENSION;
    private static final List<Character> ILLEGAL_CHARACTERS = new ArrayList<>(Arrays.asList('\\', '/', '.'));
    private static final String LOGGING_TAG = "spellbook_view_model";
    private static final String ENGLISH_SPELLS_FILENAME = "Spells.json";
    private static final String SETTINGS_FILE = "Settings.json";

    private final Application application;

    private final Settings settings;

    private final File profilesDir;
    private final File statusesDir;
    private final File createdSourcesDir;
    private final File createdSpellsDir;
    private final FileObserver profilesDirObserver;
    private final FileObserver statusesDirObserver;
    private final FileObserver createdSourcesDirObserver;
    private final FileObserver createdSpellsDirObserver;

    private final MutableLiveData<List<String>> characterNamesLD;
    private final MutableLiveData<List<String>> statusNamesLD;
    private final MutableLiveData<List<String>> createdSourceNamesLD;
    private final MutableLiveData<List<String>> createdSpellNamesLD;
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
    private List<Spell> currentSpellList;
    private final MutableLiveData<List<Spell>> currentSpellsLD;
    private final MutableLiveData<Boolean> currentSpellFavoriteLD;
    private final MutableLiveData<Boolean> currentSpellPreparedLD;
    private final MutableLiveData<Boolean> currentSpellKnownLD;
    private final MutableLiveData<Spell> currentSpellLD;
    private final MutableLiveData<Boolean> currentUseExpandedLD;
    private final MutableLiveData<Boolean> spellTableVisibleLD;

    private final SpellCodec spellCodec;

    private static final List<Integer> SORT_PROPERTY_IDS = Arrays.asList(BR.firstSortField, BR.firstSortReverse, BR.secondSortField, BR.secondSortReverse);

    private static <S,T> LiveData<T> distinctTransform(LiveData<S> source, Function<S,T> transform) {
        return Transformations.distinctUntilChanged(Transformations.map(source, transform));
    }

    public SpellbookViewModel(Application application) {
        this.application = application;

        this.profilesDir = FilesystemUtils.createFileDirectory(application, PROFILES_DIR_NAME);
        this.statusesDir = FilesystemUtils.createFileDirectory(application, STATUSES_DIR_NAME);
        this.createdSourcesDir = FilesystemUtils.createFileDirectory(application, CREATED_SOURCES_DIR_NAME);
        this.createdSpellsDir = FilesystemUtils.createFileDirectory(application, CREATED_SPELLS_DIR_NAME);

        this.spellCodec = new SpellCodec(application);

        this.currentProfileLD = new MutableLiveData<>();
        this.characterNamesLD = new MutableLiveData<>();
        this.statusNamesLD = new MutableLiveData<>();
        this.createdSourceNamesLD = new MutableLiveData<>();
        this.createdSpellNamesLD = new MutableLiveData<>();
        this.currentSpellFilterStatusLD = new MutableLiveData<>();
        this.currentSortFilterStatusLD = new MutableLiveData<>();
        this.currentSpellSlotStatusLD = new MutableLiveData<>();
        final String spellsFilename = application.getResources().getString(R.string.spells_filename);
        this.spells = loadSpellsFromFile(spellsFilename, false);
        this.currentSpellList = new ArrayList<>(spells);
        this.currentSpellsLD = new MutableLiveData<>(spells);
        this.currentSpellLD = new MutableLiveData<>();
        this.currentSpellFavoriteLD = new MutableLiveData<>();
        this.currentSpellPreparedLD = new MutableLiveData<>();
        this.currentSpellKnownLD = new MutableLiveData<>();
        this.currentUseExpandedLD = new MutableLiveData<>();
        this.spellTableVisibleLD = new MutableLiveData<>();
        updateCharacterNames();

        // Load the settings and the character profile
        this.settings = loadSettings();
        final String charName = settings.characterName();
        final List<String> names = getCharacterNames();
        if (charName != null) {
            setProfileByName(charName);
        } else if (names.size() > 0) {
            setProfileByName(names.get(0));
        }

        // If we don't already have the english spells, get them
        if (englishSpells.size() == 0) {
            englishSpells = loadSpellsFromFile(ENGLISH_SPELLS_FILENAME, true);
        }

        // Whenever a file is created or deleted in the profiles folder
        // we update the list of character names
        // Same with the sort/filter statuses, sources, and created spells
        this.profilesDirObserver = filenamesObserver(profilesDir, this::updateCharacterNames);
        this.statusesDirObserver = filenamesObserver(statusesDir, this::updateStatusNames);
        this.createdSourcesDirObserver = filenamesObserver(createdSourcesDir, this::updateCreatedSourceNames);
        this.createdSpellsDirObserver = filenamesObserver(createdSpellsDir, this::updateCreatedSpellNames);
        profilesDirObserver.startWatching();
        statusesDirObserver.startWatching();
        createdSourcesDirObserver.startWatching();
        createdSpellsDirObserver.startWatching();
    }

    private List<Spell> loadSpellsFromFile(String filename, boolean useInternalParse) {
        try {
            final JSONArray jsonArray = JSONUtils.loadJSONArrayFromAsset(application, filename);
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

    private String nameValidator(String name, int emptyItemID, int itemTypeID, List<String> existingItems) {
        if (name.isEmpty()) {
            final String emptyItem = application.getString(emptyItemID);
            return application.getString(R.string.cannot_be_empty, emptyItem);
        }

        final String itemType = application.getString(itemTypeID);
        for (Character c : ILLEGAL_CHARACTERS) {
            final String cStr = c.toString();
            if (name.contains(cStr)) {
                return application.getString(R.string.illegal_character, itemType, cStr);
            }
        }

        if (existingItems != null && existingItems.contains(name)) {
            return application.getString(R.string.duplicate_name, itemType);
        }

        return "";
    }

    String characterNameValidator(String name) {
        return nameValidator(name, R.string.character_name, R.string.name_lowercase, characterNamesLD.getValue());
    }

    String statusNameValidator(String name) {
        return nameValidator(name, R.string.status_lowercase, R.string.status_lowercase, statusNamesLD.getValue());
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

    private List<String> getNamesFromDirectory(File directory, String extension) {
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
        return names;
    }

    private void updateNamesFromDirectory(File directory, String extension, MutableLiveData<List<String>> liveData) {
        final List<String> names = getNamesFromDirectory(directory, extension);
        liveData.postValue(names);
    }

    List<String> getCharacterNames() {
        return getNamesFromDirectory(profilesDir, CHARACTER_EXTENSION);
    }

    void updateCharacterNames() {
        updateNamesFromDirectory(profilesDir, CHARACTER_EXTENSION, characterNamesLD);
    }

    void updateStatusNames() {
        updateNamesFromDirectory(statusesDir, STATUS_EXTENSION, statusNamesLD);
    }

    private void updateCreatedSourceNames() {
        updateNamesFromDirectory(createdSourcesDir, CREATED_SOURCE_EXTENSION, createdSourceNamesLD);
    }

    private void updateCreatedSpellNames() {
        updateNamesFromDirectory(createdSpellsDir, CREATED_SPELL_EXTENSION, createdSpellNamesLD);
    }

    private <T> T getDataItemByName(String name, String extension, File directory, JSONUtils.ThrowsExceptionFunction<JSONObject,T,JSONException> creator) {
        final String filename = name + extension;
        final File filepath = new File(directory, filename);
        if (!(filepath.exists() && filepath.isFile())) {
            return null;
        }

        try {
            final JSONObject json = JSONUtils.loadJSONFromData(filepath);
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
    boolean getUseExpanded() { return profile.getSortFilterStatus().getUseTashasExpandedLists(); }

    void setProfile(CharacterProfile profile) {
        this.profile = profile;
        currentProfileLD.setValue(profile);
        if (profile == null) {
            settings.setCharacterName(null);
            return;
        }

        currentSortFilterStatusLD.setValue(profile.getSortFilterStatus());
        currentSpellFilterStatusLD.setValue(profile.getSpellFilterStatus());
        currentSpellSlotStatusLD.setValue(profile.getSpellSlotStatus());
        settings.setCharacterName(profile.getName());
        setupSortFilterObserver();
        setupSpellFilterStatusObserver();
        setupSpellSlotStatusObserver();

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
                    setSortNeeded();
                } else {
                    setFilterNeeded();
                }
                if (propertyId == BR.useTashasExpandedLists) {
                    currentUseExpandedLD.setValue(sortFilterStatus.getUseTashasExpandedLists());
                }
                // Let's try this
                saveSortFilterStatus();
            }
        });
        sortNeeded = true;
        filterNeeded = true;
        modifySpellsIfAppropriate();
    }

    private void setupSpellSlotStatusObserver() {
        final SpellSlotStatus spellSlotStatus = profile.getSpellSlotStatus();
        spellSlotStatus.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (sender != spellSlotStatus) { return; }
                saveSpellSlotStatus();
            }
        });
    }

    private void setupSpellFilterStatusObserver() {
        final SpellFilterStatus spellFilterStatus = profile.getSpellFilterStatus();
        spellFilterStatus.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (sender != spellFilterStatus) { return; }
                saveSpellFilterStatus();
            }
        });
    }

    void setProfileByName(String name) {
        final CharacterProfile profile = getProfileByName(name);
        if (profile != null) {
            setProfile(profile);
        } else {
            final Context context = getContext();
            final String message = application.getString(R.string.character_load_error, name);
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    void setSortFilterStatus(SortFilterStatus sortFilterStatus) {
        setSuspendSpellListModifications(true);
        profile.setSortFilterStatus(sortFilterStatus);
        this.currentSortFilterStatusLD.setValue(sortFilterStatus);
        this.currentUseExpandedLD.setValue(sortFilterStatus.getUseTashasExpandedLists());
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
        return JSONUtils.saveAsJSON(profile, filepath);
    }

    boolean deleteProfile(CharacterProfile profile) {
        return deleteProfileByName(profile.getName());
    }

    boolean saveSortFilterStatus(SortFilterStatus status) {
        final String filename = status.getName() + STATUS_EXTENSION;
        final File filepath = new File(statusesDir, filename);
        return JSONUtils.saveAsJSON(status, filepath);
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

    private <T extends Named> boolean renameItem(String oldName, String newName, Function<String,T> getterByName, Function<String,Boolean> deleter, Consumer<T> saver) {
        final T item = getterByName.apply(oldName);
        item.setName(newName);
        saver.accept(item);
        return deleter.apply(oldName);
        // TODO: Add Toast message(s) here
    }

    boolean renameSortFilterStatus(String oldName, String newName) {
        return renameItem(oldName, newName, this::getSortFilterStatusByName, this::deleteSortFilterStatusByName, this::saveSortFilterStatus);
    }

    boolean renameProfile(String oldName, String newName) {
        final CharacterProfile profile = getProfileByName(oldName);
        profile.setName(newName);
        saveProfile(profile);
        final CharacterProfile currentProfile = currentProfileLD.getValue();
        final boolean isCurrentProfile = currentProfile != null && currentProfile.getName().equals(oldName);
        final boolean success = deleteItemByName(oldName, CHARACTER_EXTENSION, profilesDir);
        if (success && isCurrentProfile) {
            final List<String> characters = getCharacterNames();
            if (characters != null) {
                characters.stream().filter(x -> !x.equals(oldName)).findFirst().ifPresent(this::setProfileByName);
            } else {
                currentProfileLD.setValue(null);
            }
        }
        return success;
    }

    boolean deleteProfileByName(String name) {
        final boolean success = deleteItemByName(name, CHARACTER_EXTENSION, profilesDir);
        final CharacterProfile currentProfile = currentProfileLD.getValue();
        if (success && currentProfile != null && name.equals(currentProfile.getName())) {
            final List<String> characters = getCharacterNames();
            if (characters != null && characters.size() > 0) {
                characters.stream().filter(x -> !x.equals(name)).findFirst().ifPresent(this::setProfileByName);
            } else {
                setProfile(null);
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
    LiveData<List<String>> currentCreatedSourceNames() { return createdSourceNamesLD; }
    LiveData<List<String>> currentCreatedSpellNames() { return createdSpellNamesLD; }

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

    boolean addCreatedSpell(Spell spell) {
        final String filename = spell.getName() + CREATED_SPELL_EXTENSION;
        final File filepath = new File(createdSpellsDir, filename);
        return JSONUtils.saveAsJSON(spell, spellCodec::toJSON, filepath);
    }

    CharSequence getSearchQuery() { return searchQuery; }
    void setSearchQuery(CharSequence searchQuery) {
        this.searchQuery = searchQuery;
        setFilterNeeded();
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
        this.currentSpellList = filteredSpells;
        this.currentSpellsLD.setValue(filteredSpells);
        filterNeeded = false;
    }

    int getIndex(Spell spell) {
        return currentSpellList.indexOf(spell);
    }

//    private void sort() {
//        System.out.println("In sort");
//        final SortFilterStatus sortFilterStatus = profile.getSortFilterStatus();
//        final List<Pair<SortField,Boolean>> sortParameters = new ArrayList<Pair<SortField,Boolean>>() {{
//            add(new Pair<>(sortFilterStatus.getFirstSortField(), sortFilterStatus.getFirstSortReverse()));
//            add(new Pair<>(sortFilterStatus.getSecondSortField(), sortFilterStatus.getSecondSortReverse()));
//        }};
//        currentSpellList.sort(new SpellComparator(application, sortParameters));
//        currentSpellsLD.setValue(currentSpellList);
//        sortNeeded = false;
//    }

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

        // We can avoid a lot of excess sorting/filtering by waiting
        // until the spell table is visible
        // But the filtering is clearly visible switching back to the spell table fragment
        // and the excess filtering doesn't have a noticeable performance impact
        // so for now, we'll just let the list filter whenever

        //final boolean shouldModify = !suspendSpellListModifications && spellTableVisible;
        final boolean shouldModify = !suspendSpellListModifications;

        if (shouldModify) {

            // There's probably a more efficient way to do this
            // but we can leave that for later
            // The spell list isn't long enough that it'll make enough of a difference
            if (filterNeeded || sortNeeded) {
                filter();
                //sort();
                filterNeeded = false;
                sortNeeded = false;
            }
        }
    }

    void setFilterNeeded() {
        this.filterNeeded = true;
        modifySpellsIfAppropriate();
    }

    void setSortNeeded() {
        this.sortNeeded = true;
        modifySpellsIfAppropriate();
    }

    void setSpellTableVisible(boolean visible) {
        this.spellTableVisible = visible;
        this.spellTableVisibleLD.setValue(visible);
        //modifySpellsIfAppropriate();
    }

    LiveData<Boolean> spellTableCurrentlyVisible() { return spellTableVisibleLD; }

    private Settings loadSettings() {
        // Load the settings and the character profile
        try {
            final JSONObject json = JSONUtils.loadJSONFromData(application, SETTINGS_FILE);
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

    Context getContext() { return application; }

    static List<Spell> allEnglishSpells() { return englishSpells; }
}
