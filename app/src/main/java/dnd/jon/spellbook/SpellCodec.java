package dnd.jon.spellbook;

import android.content.Context;

import org.json.*;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

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


    private Spell parseSpell(JSONObject json, SpellBuilder b) throws Exception {

        // Set the values that need no/trivial parsing
        b.setID(json.getInt(ID_KEY))
            .setName(json.getString(NAME_KEY))
            .setPage(json.getInt(PAGE_KEY))
            .setSourcebook(DisplayUtils.getEnumFromResourceValue(context, Sourcebook.class, json.getString(SOURCEBOOK_KEY), Sourcebook::getCodeID, Context::getString))
            .setRange(DisplayUtils.rangeFromString(context, json.getString(RANGE_KEY)))
            .setRitual(json.optBoolean(RITUAL_KEY, false))
            .setLevel(json.getInt(LEVEL_KEY))
            .setCastingTime(DisplayUtils.castingTimeFromString(context, json.getString(CASTING_TIME_KEY)))
            .setMaterial(json.optString(MATERIAL_KEY, ""))
            .setDescription(json.getString(DESCRIPTION_KEY))
            .setHigherLevelDesc(json.getString(HIGHER_LEVEL_KEY))
            .setSchool(DisplayUtils.getEnumFromDisplayName(context, School.class, json.getString(SCHOOL_KEY)));

        // Duration, concentration, and ritual
        final String durationString = json.getString(DURATION_KEY);
        b.setDuration(DisplayUtils.durationFromString(context, durationString));
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
            b.addClass(DisplayUtils.getEnumFromDisplayName(context, CasterClass.class, classesArray.getString(i)));
        }

        // Subclasses
        if (json.has(SUBCLASSES_KEY)) {
            JSONArray subclassesArray = json.getJSONArray(SUBCLASSES_KEY);
            for (int i = 0; i < subclassesArray.length(); i++) {
                b.addSubclass(Subclass.fromDisplayName(subclassesArray.getString(i)));
            }
        }

        // Tasha's expanded classes
        if (json.has(TCE_EXPANDED_CLASSES_KEY)) {
            JSONArray expandedArray = json.getJSONArray(TCE_EXPANDED_CLASSES_KEY);
            for (int i = 0; i < expandedArray.length(); ++i) {
                b.addTashasExpandedClass(DisplayUtils.getEnumFromDisplayName(context, CasterClass.class, expandedArray.getString(i)));
            }
        }

        // Return
        return b.buildAndReset();
    }

    // Overload with no SpellBuilder
    Spell parseSpell(JSONObject obj) throws Exception {
        SpellBuilder b = new SpellBuilder();
        return parseSpell(obj, b);
    }

    List<Spell> parseSpellList(JSONArray jsonArray) throws Exception {

        final List<Spell> spells = new ArrayList<>();
        final SpellBuilder b = new SpellBuilder();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                Spell nextSpell = parseSpell(jsonArray.getJSONObject(i), b);
                spells.add(nextSpell);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return spells;
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
