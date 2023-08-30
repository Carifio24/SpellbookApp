package dnd.jon.spellbook;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.FileObserver;
import android.util.Log;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.databinding.Observable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    private static final String ENGLISH_SPELLS_FILENAME = "Spells_en.json";
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
    private final MutableLiveData<List<Source>> createdSourcesLD;
    private final MutableLiveData<List<Spell>> createdSpellsLD;
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
    private List<Spell> spells;
    private List<Spell> currentSpellList;
    private final String spellsFilename;
    private final MutableLiveData<Context> spellsContext;
    private Locale spellsLocale;
    private final MutableLiveData<List<Spell>> currentSpellsLD;
    private final MutableLiveData<Boolean> currentSpellFavoriteLD;
    private final MutableLiveData<Boolean> currentSpellPreparedLD;
    private final MutableLiveData<Boolean> currentSpellKnownLD;
    private final MutableLiveData<Spell> currentSpellLD;
    private final MutableLiveData<Boolean> currentUseExpandedLD;
    private final MutableLiveData<Boolean> spellTableVisibleLD;
    private SpellCodec spellCodec;
    private final MutableLiveData<Spell> currentEditingSpellLD;

    private final MutableLiveData<Event<String>> toastEventLD;

    private static final List<Integer> SORT_PROPERTY_IDS = Arrays.asList(BR.firstSortField, BR.firstSortReverse, BR.secondSortField, BR.secondSortReverse);

    private static final int CREATED_SPELL_ID_OFFSET = 100000;

    private static <S,T> LiveData<T> distinctTransform(LiveData<S> source, Function<S,T> transform) {
        return Transformations.distinctUntilChanged(Transformations.map(source, transform::apply));
    }

    public SpellbookViewModel(Application application) {
        this.application = application;

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        final String spellLanguageKey = application.getString(R.string.spell_language_key);
        final String spellsLocaleString = sharedPreferences.getString(spellLanguageKey, null);

        // This is kinda hacky; think of a more scalable way to do this?
        // Though once the UI and spell parsing languages a
        final boolean uninstalledLanguage = (spellsLocaleString == null) ||
                                            (spellsLocaleString.equals("pt") && !LocalizationUtils.hasPortugueseInstalled()) ||
                                            (spellsLocaleString.equals("en") && !LocalizationUtils.hasEnglishInstalled());

        if (uninstalledLanguage || !LocalizationUtils.isLanguageSupported(spellsLocaleString)) {
            this.spellsLocale = LocalizationUtils.defaultSpellLocale();
        } else {
            this.spellsLocale = new Locale(spellsLocaleString);
        }

        // If we don't have an existing value for the spell language setting
        // we set the default.
        // TODO: Can we do this in the XML? It's default locale-dependent
        if (spellsLocaleString == null) {
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(spellLanguageKey, this.spellsLocale.getLanguage());
            editor.apply();
        }
        final Context spellsContext = LocalizationUtils.getLocalizedContext(application, this.spellsLocale);
        this.spellsContext = new MutableLiveData<>(spellsContext);
        this.spellCodec = new SpellCodec(spellsContext);
        this.toastEventLD = new MutableLiveData<>();

        this.profilesDir = FilesystemUtils.createFileDirectory(application, PROFILES_DIR_NAME);
        this.statusesDir = FilesystemUtils.createFileDirectory(application, STATUSES_DIR_NAME);
        this.createdSourcesDir = FilesystemUtils.createFileDirectory(application, CREATED_SOURCES_DIR_NAME);
        this.createdSpellsDir = FilesystemUtils.createFileDirectory(application, CREATED_SPELLS_DIR_NAME);

        this.currentProfileLD = new MutableLiveData<>();
        this.characterNamesLD = new MutableLiveData<>();
        this.statusNamesLD = new MutableLiveData<>();
        this.createdSourcesLD = new MutableLiveData<>(new ArrayList<>());
        this.createdSpellsLD = new MutableLiveData<>(new ArrayList<>());
        this.currentSpellFilterStatusLD = new MutableLiveData<>();
        this.currentSortFilterStatusLD = new MutableLiveData<>();
        this.currentSpellSlotStatusLD = new MutableLiveData<>();
        initialUpdates();

        this.spellsFilename = spellsContext.getResources().getString(R.string.spells_filename);
        this.spells = loadSpellsFromFile(spellsFilename, this.spellsLocale);
        this.spells.addAll(this.getCreatedSpells());
        this.currentSpellList = new ArrayList<>(spells);
        this.currentSpellsLD = new MutableLiveData<>(spells);
        this.currentSpellLD = new MutableLiveData<>();
        this.currentSpellFavoriteLD = new MutableLiveData<>();
        this.currentSpellPreparedLD = new MutableLiveData<>();
        this.currentSpellKnownLD = new MutableLiveData<>();

        this.currentEditingSpellLD = new MutableLiveData<>();

        this.currentUseExpandedLD = new MutableLiveData<>();
        this.spellTableVisibleLD = new MutableLiveData<>();

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
            englishSpells = loadSpellsFromFile(ENGLISH_SPELLS_FILENAME, Locale.US);
        }

        // Whenever a file is created or deleted in the profiles folder
        // we update the list of character names
        // Same with the sort/filter statuses, sources, and created spells
        this.profilesDirObserver = filenamesObserver(profilesDir, this::updateCharacterNames);
        this.statusesDirObserver = filenamesObserver(statusesDir, this::updateStatusNames);
        this.createdSourcesDirObserver = filenamesObserver(createdSourcesDir, this::updateCreatedSources);
        this.createdSpellsDirObserver = filenamesObserver(createdSpellsDir, this::updateCreatedSpells);
        profilesDirObserver.startWatching();
        statusesDirObserver.startWatching();
        createdSourcesDirObserver.startWatching();
        createdSpellsDirObserver.startWatching();
    }

    void updateSpellsForLocale(Locale locale) {
        this.spellsLocale = locale;
        final Context context = LocalizationUtils.getLocalizedContext(this.getContext(), locale);
        this.spellsContext.setValue(context);
        final Resources resources = context.getResources();
        final String filename = resources.getString(R.string.spells_filename_language, locale.getLanguage());
        this.spells = loadSpellsFromFile(filename, locale);
        this.spellCodec = new SpellCodec(context);

        // If we switch locales, we need to update the current spell
        // to the version from the new locale
        final Spell spell = currentSpell().getValue();
        if (spell != null) {
            final int spellID = spell.getID();
            final Spell newSpell = this.spells.stream().filter(s -> s.getID() == spellID).findAny().orElse(null);
            currentSpellLD.setValue(newSpell);
        }
        filter();


    }

    private List<Spell> loadSpellsFromFile(String filename, Locale locale) {
        try {
            final JSONArray jsonArray = JSONUtils.loadJSONArrayFromAsset(application, filename);
            final SpellCodec codec = new SpellCodec(LocalizationUtils.getLocalizedContext(application, locale));
            final boolean useInternalParse = locale == Locale.US;
            return codec.parseSpellList(jsonArray, useInternalParse, locale);
        } catch (Exception e) {
            //TODO: Better error handling?
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private FileObserver filenamesObserver(File directory, Runnable executeOnEvent) {
        final BiConsumer<Integer,String> runOnEvent = (event, path) -> {
            switch (event) {
                case FileObserver.CREATE:
                case FileObserver.DELETE:
                    executeOnEvent.run();
            }
        };
        if (Build.VERSION.SDK_INT >= 29) {
            return new FileObserver(directory) {
                @Override
                public void onEvent(int event, @Nullable String path) {
                    runOnEvent.accept(event, path);
                }
            };
        } else {
            return new FileObserver(directory.getAbsolutePath()) {
                @Override
                public void onEvent(int event, @Nullable String path) {
                    runOnEvent.accept(event, path);
                }
            };
        }
    }

    LiveData<Spell> currentSpell() { return currentSpellLD; }
    LiveData<Boolean> currentSpellFavoriteLD() { return currentSpellFavoriteLD; }
    LiveData<Boolean> currentSpellPreparedLD() { return currentSpellPreparedLD; }
    LiveData<Boolean> currentSpellKnownLD() { return currentSpellKnownLD; }

    LiveData<Spell> currentEditingSpell() { return currentEditingSpellLD; }

    void setCurrentSpell(Spell spell) {
        currentSpellLD.setValue(spell);
        currentSpellFavoriteLD.setValue(getFavorite(spell));
        currentSpellPreparedLD.setValue(getPrepared(spell));
        currentSpellKnownLD.setValue(getKnown(spell));
    }
    void setCurrentEditingSpell(Spell spell) { currentEditingSpellLD.setValue(spell); }

    List<Spell> getAllSpells() { return spells; }
    LiveData<Context> currentSpellsContext() { return spellsContext; }
    Context getSpellContext() { return spellsContext.getValue(); }

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
        return nameValidator(name, R.string.character_name, R.string.character_lowercase, characterNamesLD.getValue());
    }

    String statusNameValidator(String name) {
        return nameValidator(name, R.string.status_lowercase, R.string.status_lowercase, statusNamesLD.getValue());
    }

    String sourceNameValidator(String name) {
        final List<String> sourceNames = Arrays.stream(Source.values()).map(s -> DisplayUtils.getDisplayName(s, getContext())).collect(Collectors.toList());
        return nameValidator(name, R.string.source_name, R.string.source, sourceNames);
    }

    String sourceAbbreviationValidator(String abbreviation) {
        final List<String> sourceAbbrs = Arrays.stream(Source.values()).map(s -> DisplayUtils.getCode(s, getContext())).collect(Collectors.toList());
        return nameValidator(abbreviation, R.string.source_abbreviation, R.string.abbreviation, sourceAbbrs);
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

    private <T, E extends Exception> List<T> getItemsFromDirectory(File directory, Predicate<File> fileFilter, SpellbookUtils.ThrowsExceptionFunction<File,T,E> itemCreator, Comparator<T> sorter) {
        final List<T> items = new ArrayList<>();
        final File[] files = directory.listFiles();
        if (files == null) { return items; }
        for (File file: files) {
            if (!fileFilter.test(file)) { continue; }
            try {
                final T item = itemCreator.apply(file);
                items.add(item);
            } catch (Exception e) {
                Log.e(LOGGING_TAG, "getItemsFromDirectory: Error creating item, file " + file.getAbsolutePath());
            }
        }
        if (sorter != null) {
            items.sort(sorter);
        }
        return items;
    }

    private <T, E extends Exception> void updateItemsFromDirectory(File directory,
                                                                   Predicate<File> fileFilter,
                                                                   SpellbookUtils.ThrowsExceptionFunction<File,T,E> itemCreator,
                                                                   Comparator<T> sorter,
                                                                   MutableLiveData<List<T>> liveData,
                                                                   boolean mainThread) {
        final List<T> items = getItemsFromDirectory(directory, fileFilter, itemCreator, sorter);
        if (mainThread) {
            liveData.setValue(items);
        } else {
            liveData.postValue(items);
        }
    }

    private <T, E extends Exception> void updateItemsFromDirectory(File directory,
                                                                   Predicate<File> fileFilter,
                                                                   SpellbookUtils.ThrowsExceptionFunction<File,T,E> itemCreator,
                                                                   Comparator<T> sorter,
                                                                   MutableLiveData<List<T>> liveData) {
        updateItemsFromDirectory(directory, fileFilter, itemCreator, sorter, liveData, false);
    }

    private static String getNameFromFile(File file, String extension) {
        final int toRemove = extension.length();
        final String filename = file.getName();
        return filename.substring(0, filename.length() - toRemove);
    }

    private List<String> getNamesFromDirectory(File directory, String extension) {
        return getItemsFromDirectory(directory, SpellbookUtils.extensionFilter(extension), (f) -> getNameFromFile(f, extension), String::compareToIgnoreCase);
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

    private void updateCreatedSources(boolean mainThread) {
        updateItemsFromDirectory(createdSourcesDir,
                SpellbookUtils.extensionFilter(CREATED_SOURCE_EXTENSION),
                JSONUtils::sourceFromJSON,
                null,
                createdSourcesLD,
                mainThread);
    }

    private void updateCreatedSources() { updateCreatedSources(false); }

    private Spell spellFromFile(File file) throws Exception {
        final SpellBuilder builder = new SpellBuilder(getContext(), LocalizationUtils.getLocale());
        final JSONObject json = JSONUtils.loadJSONFromData(file);
        if (json == null) {
            throw new JSONException("Error loading spell JSON");
        }
        return spellCodec.parseSpell(json, builder, false);
    }

    private List<Spell> getCreatedSpells() {
        return getItemsFromDirectory(createdSpellsDir, SpellbookUtils.extensionFilter(CREATED_SPELL_EXTENSION), this::spellFromFile, null);
    }

    private void updateCreatedSpells(boolean mainThread) {
        updateItemsFromDirectory(createdSpellsDir,
                SpellbookUtils.extensionFilter(CREATED_SPELL_EXTENSION),
                this::spellFromFile,
                null,
                createdSpellsLD,
                mainThread);
    }

    private void updateCreatedSpells() { updateCreatedSpells(false); }

    private <T> T getDataItemByName(String name, String extension, File directory, SpellbookUtils.ThrowsExceptionFunction<JSONObject,T,JSONException> creator) {
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
                //System.out.println(propertyId);
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
            toastEventLD.postValue(new Event<>(message));
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

    void initialUpdates() {
        updateCharacterNames();
        updateCreatedSources(true);
        updateCreatedSpells(true);
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
    LiveData<List<Source>> currentCreatedSources() { return createdSourcesLD; }
    LiveData<List<Spell>> currentCreatedSpells() { return createdSpellsLD; }

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

    boolean addCreatedSource(Source source) {
        final String filename = DisplayUtils.getCode(source, getContext()) + CREATED_SOURCE_EXTENSION;
        final File filepath = new File(createdSourcesDir, filename);
        return JSONUtils.saveAsJSON(source, (src) -> JSONUtils.asJSON(src, getContext()), filepath);
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
        if (liveData != null) {
            liveData.setValue(value);
        }
    }
    void setFavorite(Spell spell, boolean favorite) {
        final MutableLiveData<Boolean> liveData = spell.equals(currentSpellLD.getValue()) ? currentSpellFavoriteLD : null;
        setProperty(SpellFilterStatus::setFavorite, spell, favorite, liveData);
    }
    void setPrepared(Spell spell, boolean prepared) {
        final MutableLiveData<Boolean> liveData = spell.equals(currentSpellLD.getValue()) ? currentSpellPreparedLD : null;
        setProperty(SpellFilterStatus::setPrepared, spell, prepared, liveData);
    }
    void setKnown(Spell spell, boolean known) {
        final MutableLiveData<Boolean> liveData = spell.equals(currentSpellLD.getValue()) ? currentSpellKnownLD : null;
        setProperty(SpellFilterStatus::setKnown, spell, known, liveData);
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

    LiveData<Event<String>> currentToastEvent() { return toastEventLD; }

    static List<Spell> allEnglishSpells() { return englishSpells; }

    void castSpell(Spell spell, int level) {
        castSpell(spell, level, true);
    }

    void castSpell(Spell spell) {
        castSpell(spell, spell.getLevel(), false);
    }

    private void castSpell(Spell spell, int level, boolean levelInMessage) {
        final SpellSlotStatus status = getSpellSlotStatus();
        final Context context = getContext();
        final String name = profile.getName();

        String message;
        if (status.getTotalSlots(level) == 0) {
            message = context.getString(R.string.no_slots_of_level_or_above, name, level);
        } else if (status.getTotalSlots(level) == 0 || status.getAvailableSlots(level) == 0) {
            message = context.getString(R.string.no_slots_remaining_of_level_or_above, name, level);
        } else if (levelInMessage) {
            message = context.getString(R.string.cast_spell_with_level, spell.getName(), level);
        } else {
            message = context.getString(R.string.cast_spell, spell.getName());
        }

        status.useSlot(level);
        toastEventLD.postValue(new Event<>(message));
    }

    private Set<Integer> createdSpellIDs() {
        final List<Spell> createdSpells = createdSpellsLD.getValue();
        if (createdSpells == null) {
            return new TreeSet<>();
        }
        return createdSpells.stream().map(Spell::getID).collect(Collectors.toSet());
    }

    // We distinguish official and created spell IDs by adding an offset that we assume will be
    // larger than the number of official spells.
    // We can't just start the created spell IDs after the official spell ones, as the list of
    // official spells will likely grow.
    // This feels a bit hacky (it would be nicer to have these sorts of constraints built into
    // the data structure), but I'm not sure what a better way to do this would be, since a lot of
    // the app infrastructure is looking to grab spells by their (integer) IDs
    int newSpellID() {
        final Set<Integer> ids = createdSpellIDs();
        int id = CREATED_SPELL_ID_OFFSET;
        while (ids.contains(id)) {
            id += 1;
        }
        return id;
    }

}
