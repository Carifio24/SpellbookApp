package dnd.jon.spellbook;

import org.json.*;
import java.util.ArrayList;

class SpellParser {

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
    private static final String SCHOOL_NAME_KEY = NAME_KEY;
    private static final String CLASSES_KEY = "classes";
    private static final String CLASS_NAME_KEY = NAME_KEY;
    private static final String SUBCLASSES_KEY = "subclasses";
    private static final String SUBCLASS_NAME_KEY = NAME_KEY;

    private static final String CONCENTRATION_PREFIX = "Up to";


    private static Spell parseSpell(JSONObject obj, SpellBuilder b) throws JSONException, Exception {

        // Objects to reuse
        StringBuilder jsb = new StringBuilder();
        JSONObject jso;
        JSONArray jarr;

        // Set the values that need no/trivial parsing
        b.setName(obj.getString(NAME_KEY));
        String locationStr = obj.getString(PAGE_KEY);
        String[] locationPieces = locationStr.split(" ", 0);
        final int page = Integer.parseInt(locationPieces[1]);
        b.setPage(page);
        final String code = locationPieces[0].toUpperCase();
        final Sourcebook source = Sourcebook.fromCode(code);
        b.setSourcebook(source);
        b.setDuration(Duration.fromString(obj.getString(DURATION_KEY)));
        b.setRange(Range.fromString(obj.getString(RANGE_KEY)));
        if (obj.has(RITUAL_KEY)) {
            b.setRitual(Util.yn_to_bool(obj.getString(RITUAL_KEY)));
        } else {
            b.setRitual(false);
        }
        if (obj.getString(DURATION_KEY).startsWith(CONCENTRATION_PREFIX)) {
            b.setConcentration(true);
        } else if (obj.has(CONCENTRATION_KEY)) {
            b.setConcentration(Util.yn_to_bool(obj.getString(CONCENTRATION_KEY)));
        } else {
            b.setConcentration(false);
        }
        b.setLevel(obj.getInt(LEVEL_KEY));
        b.setCastingTime(CastingTime.fromString(obj.getString(CASTING_TIME_KEY)));

        // Material, if necessary
        if (obj.has(MATERIAL_KEY)) {
            b.setMaterial(obj.getString(MATERIAL_KEY));
        } else {
            b.setMaterial("");
        }

        // Components
        boolean[] components = {false, false, false};
        jarr = obj.getJSONArray(COMPONENTS_KEY);
        for (int i = 0; i < jarr.length(); i++) {
            final char c = jarr.getString(i).charAt(0);
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
//            final String comp = jarr.getString(i);
//            if (comp.equals("V")) { components[0] = true; }
//            else if (comp.equals("S")) {components[1] = true;}
//            else if (comp.equals("M")) {components[2] = true;}
        }
        b.setComponents(components);

        // Description
        boolean firstAdded = false;
        jarr = obj.getJSONArray(DESCRIPTION_KEY);
        for (int i = 0; i < jarr.length(); i++) {
            if (!firstAdded) {
                firstAdded = true;
            } else {
                jsb.append("\n");
            }
            jsb.append(jarr.getString(i));
        }
        b.setDescription(jsb.toString());

        // Higher level description
        jsb.setLength(0);
        firstAdded = false;
        if (obj.has(HIGHER_LEVEL_KEY)) {

            jarr = obj.getJSONArray(HIGHER_LEVEL_KEY);
            for (int i = 0; i < jarr.length(); i++) {
                if (!firstAdded) {
                    firstAdded = true;
                } else {
                    jsb.append("\n");
                }
                jsb.append(jarr.getString(i));
            }
        }
        b.setHigherLevelDesc(jsb.toString());

        // School
        jso = obj.getJSONObject(SCHOOL_KEY);
        String schoolName = jso.getString(SCHOOL_NAME_KEY);
        b.setSchool(School.fromDisplayName(schoolName));

        // Classes
        ArrayList<CasterClass> classes = new ArrayList<>();
        jarr = obj.getJSONArray(CLASSES_KEY);
        for (int i = 0; i < jarr.length(); i++) {
            String name;
            try {
                jso = jarr.getJSONObject(i);
                name = jso.getString(CLASS_NAME_KEY);
            } catch (JSONException e) {
                name = jarr.getString(i);
            }
            classes.add(CasterClass.fromDisplayName(name));
        }
        b.setClasses(classes);

        // Subclasses
        ArrayList<SubClass> subclasses = new ArrayList<>();
        if (obj.has(SUBCLASSES_KEY)) {
            jarr = obj.getJSONArray(SUBCLASSES_KEY);
            for (int i = 0; i < jarr.length(); i++) {
                jso = jarr.getJSONObject(i);
                String name = jso.getString(SUBCLASS_NAME_KEY);
                subclasses.add(SubClass.fromDisplayName(name));
            }
        }
        b.setSubclasses(subclasses);

        // Return
        return b.buildAndReset();
    }

    // Overload with no SpellBuilder
    static Spell parseSpell(JSONObject obj) throws JSONException, Exception {
        SpellBuilder b = new SpellBuilder();
        return parseSpell(obj, b);
    }

    static ArrayList<Spell> parseSpellList(JSONArray jsonArray) throws Exception {

        ArrayList<Spell> spells = new ArrayList<>();
        SpellBuilder b = new SpellBuilder();

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

}
