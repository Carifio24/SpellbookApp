package dnd.jon.spellbook;

import org.json.*;

import java.util.List;
import java.util.ArrayList;

class SpellCodec {

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
    private static final String SOURCEBOOK_KEY = "sourcebook";

    private static final String CONCENTRATION_PREFIX = "Up to";
    private static final String[] COMPONENT_STRINGS = { "V", "S", "M" };


    private static Spell parseSpell(JSONObject json, SpellBuilder b) throws Exception {

        // Set the values that need no/trivial parsing
        b.setName(json.getString(NAME_KEY))
            .setPage(json.getInt(PAGE_KEY))
            //.setSource(Source.fromCode(json.getString(SOURCEBOOK_KEY)))
            .setRange(Range.fromString(json.getString(RANGE_KEY)))
            .setRitual(json.optBoolean(RITUAL_KEY, false))
            .setLevel(json.getInt(LEVEL_KEY))
            .setCastingTime(CastingTime.fromString(json.getString(CASTING_TIME_KEY)))
            .setMaterials(json.optString(MATERIAL_KEY, ""))
            .setDescription(json.getString(DESCRIPTION_KEY))
            .setHigherLevelDesc(json.getString(HIGHER_LEVEL_KEY))
            .setSchool(School.fromDisplayName(json.getString(SCHOOL_KEY)));

        // Duration, concentration, and ritual
        final String durationString = json.getString(DURATION_KEY);
        b.setDuration(Duration.fromString(durationString));
        boolean concentration = false;
        if (durationString.startsWith(CONCENTRATION_PREFIX)) {
            concentration = true;
        } else if (json.has(CONCENTRATION_KEY)) {
            concentration = json.getBoolean(CONCENTRATION_KEY);
        }
        b.setConcentration(concentration);


        // Components
        JSONArray componentsArray = json.getJSONArray(COMPONENTS_KEY);
        for (int i = 0; i < componentsArray.length(); ++i) {
            final char c = componentsArray.getString(i).charAt(0);
            switch (c) {
                case 'V':
                    b.setVerbalComponent(true);
                    break;
                case 'S':
                    b.setSomaticComponent(true);
                    break;
                case 'M':
                    b.setMaterialComponent(true);
            }
        }

        // Classes
        List<CasterClass> classes = new ArrayList<>();
        JSONArray classesArray = json.getJSONArray(CLASSES_KEY);
        for (int i = 0; i < classesArray.length(); i++) {
            classes.add(CasterClass.fromDisplayName(classesArray.getString(i)));
        }
        b.setClasses(classes);

        // Subclasses
        List<Subclass> subclasses = new ArrayList<>();
        if (json.has(SUBCLASSES_KEY)) {
            JSONArray subclassesArray = json.getJSONArray(SUBCLASSES_KEY);
            for (int i = 0; i < subclassesArray.length(); i++) {
                subclasses.add(Subclass.fromDisplayName(subclassesArray.getString(i)));
            }
        }
        b.setSubclasses(subclasses);

        // Return
        return b.buildAndReset();
    }

    // Overload with no SpellBuilder
    static Spell parseSpell(JSONObject obj) throws Exception {
        SpellBuilder b = new SpellBuilder();
        return parseSpell(obj, b);
    }

    static List<Spell> parseSpellList(JSONArray jsonArray) throws Exception {

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

    static JSONObject toJSON(Spell s) throws JSONException {

        final JSONObject json = new JSONObject();

        json.put(NAME_KEY, s.getName());
        json.put(DESCRIPTION_KEY, s.getDescription());
        json.put(HIGHER_LEVEL_KEY, s.getHigherLevel());
        json.put(PAGE_KEY, s.getPage());
        json.put(RANGE_KEY, s.getRange().string());
        json.put(MATERIAL_KEY, s.getMaterials());
        json.put(RITUAL_KEY, s.getRitual());
        json.put(DURATION_KEY, s.getDuration().string());
        json.put(CONCENTRATION_KEY, s.getConcentration());
        json.put(CASTING_TIME_KEY, s.getCastingTime().string());
        json.put(LEVEL_KEY, s.getLevel());
        json.put(SCHOOL_KEY, s.getSchoolName());
        //json.put(SOURCEBOOK_KEY, s.getSourcebookCode());

        final JSONArray components = new JSONArray();
        final boolean[] spellComponents = new boolean[]{ s.hasVerbalComponent(), s.hasSomaticComponent(), s.hasMaterialComponent() };
        for (int i = 0; i < spellComponents.length; ++i) {
            if (spellComponents[i]) {
                components.put(COMPONENT_STRINGS[i]);
            }
        }
        json.put(COMPONENTS_KEY, components);

        JSONArray classes = new JSONArray();
        List<CasterClass> spellClasses = s.getClasses();
        for (CasterClass cc : spellClasses) {
            classes.put(cc.getDisplayName());
        }
        json.put(CLASSES_KEY, classes);

        JSONArray subclasses = new JSONArray();
        List<Subclass> spellSubclasses = s.getSubclasses();
        for (Subclass sc : spellSubclasses) {
            subclasses.put(sc.getDisplayName());
        }
        json.put(SUBCLASSES_KEY, subclasses);

        return json;
    }

}
