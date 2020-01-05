package dnd.jon.spellbook;

import org.json.*;
import java.util.ArrayList;

class SpellParser {

    static Spell parseSpell(JSONObject obj, SpellBuilder b) throws JSONException, Exception {

        // Objects to reuse
        StringBuilder jsb = new StringBuilder();
        JSONObject jso;
        JSONArray jarr;

        // Set the values that need no/trivial parsing
        b.setName(obj.getString("name"));
        String locationStr = obj.getString("page");
        String[] locationPieces = locationStr.split(" ", 0);
        int page = Integer.parseInt(locationPieces[1]);
        b.setPage(page);
        String code = locationPieces[0].toUpperCase();
        Sourcebook source = Sourcebook.fromCode(code);
        b.setSourcebook(source);
        b.setDuration(Duration.fromString(obj.getString("duration")));
        b.setRange(Range.fromString(obj.getString("range")));
        if (obj.has("ritual")) {
            b.setRitual(Util.yn_to_bool(obj.getString("ritual")));
        } else {
            b.setRitual(false);
        }
        if (obj.getString("duration").startsWith("Up to")) {
            b.setConcentration(true);
        } else if (obj.has("concentration")) {
            b.setConcentration(Util.yn_to_bool(obj.getString("concentration")));
        } else {
            b.setConcentration(false);
        }
        b.setLevel(obj.getInt("level"));
        b.setCastingTime(obj.getString("casting_time"));

        // Material, if necessary
        if (obj.has("material")) {
            b.setMaterial(obj.getString("material"));
        } else {
            b.setMaterial("");
        }

        // Components
        boolean[] components = {false, false, false};
        jarr = obj.getJSONArray("components");
        for (int i = 0; i < jarr.length(); i++) {
            String comp = jarr.getString(i);
            if (comp.equals("V")) { components[0] = true; }
            else if (comp.equals("S")) {components[1] = true;}
            else if (comp.equals("M")) {components[2] = true;}
        }
        b.setComponents(components);

        // Description
        boolean firstAdded = false;
        jarr = obj.getJSONArray("desc");
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
        if (obj.has("higher_level")) {

            jarr = obj.getJSONArray("higher_level");
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
        jso = obj.getJSONObject("school");
        String schoolName = jso.getString("name");
        b.setSchool(School.fromDisplayName(schoolName));

        // Classes
        ArrayList<CasterClass> classes = new ArrayList<>();
        jarr = obj.getJSONArray("classes");
        for (int i = 0; i < jarr.length(); i++) {
            String name;
            try {
                jso = jarr.getJSONObject(i);
                name = jso.getString("name");
            } catch (JSONException e) {
                name = jarr.getString(i);
            }
            classes.add(CasterClass.fromDisplayName(name));
        }
        b.setClasses(classes);

        // Subclasses
        ArrayList<SubClass> subclasses = new ArrayList<>();
        if (obj.has("subclasses")) {
            jarr = obj.getJSONArray("subclasses");
            for (int i = 0; i < jarr.length(); i++) {
                jso = jarr.getJSONObject(i);
                String name = jso.getString("name");
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

    static ArrayList<Spell> parseSpellList(JSONArray jarr) throws Exception {

        ArrayList<Spell> spells = new ArrayList<>();
        SpellBuilder b = new SpellBuilder();

        try {
            for (int i = 0; i < jarr.length(); i++) {
                Spell nextSpell = parseSpell(jarr.getJSONObject(i), b);
                spells.add(nextSpell);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return spells;
    }

}
