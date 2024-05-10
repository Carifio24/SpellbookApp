package dnd.jon.spellbook;

import android.content.Context;

import androidx.annotation.Nullable;

import org.json.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

class SpellCodec {

    private static final String ID_KEY = "id";
    private static final String NAME_KEY = "name";
    private static final String PAGE_KEY = "page";
    private static final String DURATION_KEY = "duration";
    private static final String RANGE_KEY = "range";
    private static final String RITUAL_KEY = "ritual";
    private static final String CONCENTRATION_KEY = "concentration";
    private static final String LEVEL_KEY = "level";
    private static final String CASTING_TIME_KEY = "casting_time";
    private static final String MATERIAL_KEY = "material";
    private static final String ROYALTY_KEY = "royalty";
    private static final String COMPONENTS_KEY = "components";
    private static final String DESCRIPTION_KEY = "desc";
    private static final String HIGHER_LEVEL_KEY = "higher_level";
    private static final String SCHOOL_KEY = "school";
    private static final String CLASSES_KEY = "classes";
    private static final String SUBCLASSES_KEY = "subclasses";
    private static final String TCE_EXPANDED_CLASSES_KEY = "tce_expanded_classes";
    private static final String SOURCEBOOK_KEY = "sourcebook";
    private static final String LOCATIONS_KEY = "locations";

    private static final String[] COMPONENT_STRINGS = { "V", "S", "M" };

    // TODO: Is there a better way to do this?
    // It doesn't seem like it
    private static final Map<String,Integer> concentrationPrefixMap = new HashMap<>() {{
        put(Locale.US.getLanguage(), R.string.concentration_prefix_en);
        put("pt", R.string.concentration_prefix_pt);
    }};

    private final String concentrationPrefix;
    private final Context context;
    SpellCodec(Context context) {
        final Locale locale = SpellbookUtils.coalesce(context.getResources().getConfiguration().getLocales().get(0), Locale.US);
        this.context = context;
        this.concentrationPrefix = context.getString(concentrationPrefixMap.getOrDefault(locale, R.string.concentration_prefix_en));
    }


    Spell parseSpell(JSONObject json, SpellBuilder b, boolean useInternal) throws JSONException {

        // Set the values that need no/trivial parsing
        //System.out.println(json.toString());
        //System.out.println(json.getString(NAME_KEY));
        //System.out.println("Using internal: " + useInternal);

        // Value getters
        final Function<String, Source> sourcebookGetter = useInternal ? Source::fromInternalName : (string) -> DisplayUtils.sourceFromCode(context, string);
        final Function<String, Range> rangeGetter = useInternal ? Range::fromInternalString : (string) -> DisplayUtils.rangeFromString(context, string);
        final Function<String, CastingTime> castingTimeGetter = useInternal ? CastingTime::fromInternalString : (string) -> DisplayUtils.castingTimeFromString(context, string);
        final Function<String, School> schoolGetter = useInternal ? School::fromInternalName : (string) -> DisplayUtils.getEnumFromDisplayName(context, School.class, string);
        final Function<String, Duration> durationGetter = useInternal ? Duration::fromInternalString : (string) -> DisplayUtils.durationFromString(context, string);
        final Function<String, CasterClass> classGetter = useInternal ? CasterClass::fromInternalName : (string) -> DisplayUtils.getEnumFromDisplayName(context, CasterClass.class, string);
        final Function<String, Subclass> subclassGetter = Subclass::fromDisplayName;

        b.setID(json.getInt(ID_KEY))
            .setName(json.getString(NAME_KEY))
            .setRange(rangeGetter.apply(json.getString(RANGE_KEY)))
            .setRitual(json.optBoolean(RITUAL_KEY, false))
            .setLevel(json.getInt(LEVEL_KEY))
            .setCastingTime(castingTimeGetter.apply(json.getString(CASTING_TIME_KEY)))
            .setMaterial(json.optString(MATERIAL_KEY, ""))
            .setRoyalty(json.optString(ROYALTY_KEY, ""))
            .setDescription(json.getString(DESCRIPTION_KEY))
            .setHigherLevelDesc(json.getString(HIGHER_LEVEL_KEY))
            .setSchool(schoolGetter.apply(json.getString(SCHOOL_KEY)));

        // Locations
        final JSONArray locationsArray = json.getJSONArray(LOCATIONS_KEY);
        for (int i = 0; i < locationsArray.length(); i++) {
            final JSONObject location = locationsArray.getJSONObject(i);
            final Source source = sourcebookGetter.apply(location.getString(SOURCEBOOK_KEY));
            if (source != null) {
                final Integer page = location.getInt(PAGE_KEY);
                b.addLocation(source, page);
            }
        }

        // Duration, concentration, and ritual
        final String durationString = json.getString(DURATION_KEY);
        b.setDuration(durationGetter.apply(durationString));
        boolean concentration = false;
        if (json.has(CONCENTRATION_KEY)) {
            concentration = json.getBoolean(CONCENTRATION_KEY);
        } else if (durationString.startsWith(concentrationPrefix)) {
            concentration = true;
        }
        b.setConcentration(concentration);


        // Components
        boolean[] components = { false, false, false, false };
        JSONArray componentsArray = json.getJSONArray(COMPONENTS_KEY);
        for (int i = 0; i < componentsArray.length(); ++i) {
            final char c = componentsArray.getString(i).charAt(0);
            switch (c) {
                case 'V':
                    components[0] = true;
                    break;
                case 'S':
                    components[1] = true;
                    break;
                case 'M':
                    components[2] = true;
                    break;
                case 'R':
                    components[3] = true;
            }
        }
        b.setComponents(components);

        // Classes
        JSONArray classesArray = json.getJSONArray(CLASSES_KEY);
        for (int i = 0; i < classesArray.length(); i++) {
            b.addClass(classGetter.apply(classesArray.getString(i)));
        }

        // Subclasses
        if (json.has(SUBCLASSES_KEY)) {
            JSONArray subclassesArray = json.getJSONArray(SUBCLASSES_KEY);
            for (int i = 0; i < subclassesArray.length(); i++) {
                b.addSubclass(subclassGetter.apply(subclassesArray.getString(i)));
            }
        }

        // Tasha's expanded classes
        if (json.has(TCE_EXPANDED_CLASSES_KEY)) {
            JSONArray expandedArray = json.getJSONArray(TCE_EXPANDED_CLASSES_KEY);
            for (int i = 0; i < expandedArray.length(); ++i) {
                b.addTashasExpandedClass(classGetter.apply(expandedArray.getString(i)));
            }
        }

        // Return
        return b.buildAndReset();
    }

    // TODO: This is kinda gross - try to find a way not to need the useInternal
    // It should be possible to just replace it with the locale
    List<Spell> parseSpellList(@Nullable JSONArray jsonArray, boolean useInternal, Locale locale) throws Exception {

        final List<Spell> spells = new ArrayList<>();
        final SpellBuilder b = useInternal ? new SpellBuilder(context, Locale.US) : new SpellBuilder(context, locale);

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                final Spell nextSpell = parseSpell(jsonArray.getJSONObject(i), b, useInternal);
                spells.add(nextSpell);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return spells;
    }

    List<Spell> parseSpellList(JSONArray jsonArray, Locale locale) throws Exception {
        return parseSpellList(jsonArray, false, locale);
    }

    List<Spell> parseSpellList(JSONArray jsonArray, boolean useInternalParse) throws Exception {
        return parseSpellList(jsonArray, useInternalParse, LocalizationUtils.getLocale());
    }

    List<Spell> parseSpellList(JSONArray jsonArray) throws Exception {
        return parseSpellList(jsonArray, false, LocalizationUtils.getLocale());
    }

    JSONObject toJSON(Spell spell, Context context) throws JSONException {

        final JSONObject json = new JSONObject();

        json.put(ID_KEY, spell.getID());
        json.put(NAME_KEY, spell.getName());
        json.put(DESCRIPTION_KEY, spell.getDescription());
        json.put(HIGHER_LEVEL_KEY, spell.getHigherLevel());
        json.put(RANGE_KEY, DisplayUtils.string(context, spell.getRange()));
        json.put(MATERIAL_KEY, spell.getMaterial());
        json.put(ROYALTY_KEY, spell.getRoyalty());
        json.put(RITUAL_KEY, spell.getRitual());
        json.put(DURATION_KEY, DisplayUtils.string(context, spell.getDuration()));
        json.put(CONCENTRATION_KEY, spell.getConcentration());
        json.put(CASTING_TIME_KEY, DisplayUtils.string(context, spell.getCastingTime()));
        json.put(LEVEL_KEY, spell.getLevel());
        json.put(SCHOOL_KEY, context.getString(spell.getSchool().getDisplayNameID()));

        int i = 0;
        final JSONArray locations = new JSONArray();
        for (Map.Entry<Source,Integer> entry: spell.getLocations().entrySet()) {
            final JSONObject location = new JSONObject();
            location.put(SOURCEBOOK_KEY, DisplayUtils.getCode(entry.getKey(), context));
            location.put(PAGE_KEY, entry.getValue());
            locations.put(i++, location);
        }
        json.put(LOCATIONS_KEY, locations);

        final JSONArray components = new JSONArray();
        final boolean[] spellComponents = spell.getComponents();
        for (i = 0; i < spellComponents.length; ++i) {
            if (spellComponents[i]) {
                components.put(COMPONENT_STRINGS[i]);
            }
        }
        json.put(COMPONENTS_KEY, components);

        JSONArray classes = new JSONArray();
        Collection<CasterClass> spellClasses = spell.getClasses();
        for (CasterClass cc : spellClasses) {
            classes.put(DisplayUtils.getDisplayName(context, cc));
        }
        json.put(CLASSES_KEY, classes);

        JSONArray subclasses = new JSONArray();
        Collection<Subclass> spellSubclasses = spell.getSubclasses();
        for (Subclass sc : spellSubclasses) {
            subclasses.put(sc.getDisplayName());
        }
        json.put(SUBCLASSES_KEY, subclasses);

        JSONArray expandedClasses = new JSONArray();
        Collection<CasterClass> spellExpandedClasses = spell.getTashasExpandedClasses();
        for (CasterClass cc : spellExpandedClasses) {
            expandedClasses.put(DisplayUtils.getDisplayName(context, cc));
        }
        json.put(TCE_EXPANDED_CLASSES_KEY, expandedClasses);

        return json;
    }

}
