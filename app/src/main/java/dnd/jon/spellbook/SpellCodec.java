package dnd.jon.spellbook;

import android.content.Context;

import org.json.*;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
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
    private static final String COMPONENTS_KEY = "components";
    private static final String DESCRIPTION_KEY = "desc";
    private static final String HIGHER_LEVEL_KEY = "higher_level";
    private static final String SCHOOL_KEY = "school";
    private static final String CLASSES_KEY = "classes";
    private static final String SUBCLASSES_KEY = "subclasses";
    private static final String TCE_EXPANDED_CLASSES_KEY = "tce_expanded_classes";
    private static final String SOURCEBOOK_KEY = "sourcebook";

    private static final String[] COMPONENT_STRINGS = { "V", "S", "M" };

    private final String concentrationPrefix;
    private final Context context;
    SpellCodec(Context context) {
        this.context = context;
        concentrationPrefix = context.getString(R.string.concentration_prefix);
    }


    private Spell parseSpell(JSONObject json, SpellBuilder b, boolean useInternal) throws Exception {

        // Set the values that need no/trivial parsing
        //System.out.println(json.toString());

        // Value getters
        final Function<String, Sourcebook> sourcebookGetter = useInternal ? Sourcebook::fromInternalName : (string) ->  DisplayUtils.getEnumFromResourceValue(context, Sourcebook.class, string, Sourcebook::getCodeID, Context::getString);
        final Function<String, Range> rangeGetter = useInternal ? Range::fromInternalString : (string) -> DisplayUtils.rangeFromString(context, string);
        final Function<String, CastingTime> castingTimeGetter = useInternal ? CastingTime::fromInternalString : (string) -> DisplayUtils.castingTimeFromString(context, string);
        final Function<String, School> schoolGetter = useInternal ? School::fromInternalName : (string) -> DisplayUtils.getEnumFromDisplayName(context, School.class, string);
        final Function<String, Duration> durationGetter = useInternal ? Duration::fromInternalString : (string) -> DisplayUtils.durationFromString(context, string);
        final Function<String, CasterClass> classGetter = useInternal ? CasterClass::fromInternalName : (string) -> DisplayUtils.getEnumFromDisplayName(context, CasterClass.class, string);
        final Function<String, Subclass> subclassGetter = Subclass::fromDisplayName;

        b.setID(json.getInt(ID_KEY))
            .setName(json.getString(NAME_KEY))
            .setPage(json.getInt(PAGE_KEY))
            .setSourcebook(sourcebookGetter.apply(json.getString(SOURCEBOOK_KEY)))
            .setRange(rangeGetter.apply(json.getString(RANGE_KEY)))
            .setRitual(json.optBoolean(RITUAL_KEY, false))
            .setLevel(json.getInt(LEVEL_KEY))
            .setCastingTime(castingTimeGetter.apply(json.getString(CASTING_TIME_KEY)))
            .setMaterial(json.optString(MATERIAL_KEY, ""))
            .setDescription(json.getString(DESCRIPTION_KEY))
            .setHigherLevelDesc(json.getString(HIGHER_LEVEL_KEY))
            .setSchool(schoolGetter.apply(json.getString(SCHOOL_KEY)));

        // Duration, concentration, and ritual
        final String durationString = json.getString(DURATION_KEY);
        b.setDuration(durationGetter.apply(durationString));
        boolean concentration = false;
        if (durationString.startsWith(concentrationPrefix)) {
            concentration = true;
        } else if (json.has(CONCENTRATION_KEY)) {
            concentration = json.getBoolean(CONCENTRATION_KEY);
        }
        b.setConcentration(concentration);


        // Components
        boolean[] components = { false, false, false };
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

    List<Spell> parseSpellList(JSONArray jsonArray, boolean useInternal) throws Exception {

        final List<Spell> spells = new ArrayList<>();
        final SpellBuilder b = new SpellBuilder();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                Spell nextSpell = parseSpell(jsonArray.getJSONObject(i), b, useInternal);
                spells.add(nextSpell);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return spells;
    }

    List<Spell> parseSpellList(JSONArray jsonArray) throws Exception {
        return parseSpellList(jsonArray, false);
    }

    JSONObject toJSON(Spell spell) throws JSONException {

        final JSONObject json = new JSONObject();

        json.put(ID_KEY, spell.getID());
        json.put(NAME_KEY, spell.getName());
        json.put(DESCRIPTION_KEY, spell.getDescription());
        json.put(HIGHER_LEVEL_KEY, spell.getHigherLevel());
        json.put(PAGE_KEY, spell.getPage());
        json.put(RANGE_KEY, spell.getRange().internalString());
        json.put(MATERIAL_KEY, spell.getMaterial());
        json.put(RITUAL_KEY, spell.getRitual());
        json.put(DURATION_KEY, spell.getDuration().internalString());
        json.put(CONCENTRATION_KEY, spell.getConcentration());
        json.put(CASTING_TIME_KEY, spell.getCastingTime().internalString());
        json.put(LEVEL_KEY, spell.getLevel());
        json.put(SCHOOL_KEY, spell.getSchool().getInternalName());
        json.put(SOURCEBOOK_KEY, DisplayUtils.getProperty(context, spell.getSourcebook(), Sourcebook::getCodeID, Context::getString));

        final JSONArray components = new JSONArray();
        final boolean[] spellComponents = spell.getComponents();
        for (int i = 0; i < spellComponents.length; ++i) {
            if (spellComponents[i]) {
                components.put(COMPONENT_STRINGS[i]);
            }
        }
        json.put(COMPONENTS_KEY, components);

        JSONArray classes = new JSONArray();
        Collection<CasterClass> spellClasses = spell.getClasses();
        for (CasterClass cc : spellClasses) {
            classes.put(cc.getInternalName());
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
            expandedClasses.put(cc.getInternalName());
        }
        json.put(TCE_EXPANDED_CLASSES_KEY, expandedClasses);

        return json;
    }

}
