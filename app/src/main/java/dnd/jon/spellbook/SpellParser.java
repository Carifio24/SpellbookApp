package dnd.jon.spellbook;

import org.json.*;
import java.util.ArrayList;
import java.util.Arrays;

class SpellParser {

    static Spell parseSpell(JSONObject obj, SpellBuilder b) throws JSONException, Exception {

        // Objects to reuse
        String jStr;
        JSONObject jso;
        JSONArray jarr;

        // Set the values that need no/trivial parsing
        b.setName(obj.getString("name"));
        jStr = obj.getString("page");
        String[] locationPieces = jStr.split(" ", 0);
        int page = Integer.parseInt(locationPieces[1]);
        b.setPage(page);
        Sourcebook source = sourcebookFromName(locationPieces[0]);
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
            if (comp.equals("V")) {components[0] = true;}
            else if (comp.equals("S")) {components[1] = true;}
            else if (comp.equals("M")) {components[2] = true;}
        }
        b.setComponents(components);

        // Description
        String jstr = "";
        boolean firstAdded = false;
        jarr = obj.getJSONArray("desc");
        for (int i = 0; i < jarr.length(); i++) {
            if (!firstAdded) {
                firstAdded = true;
            } else {
                jstr += "\n";
            }
            jstr += jarr.getString(i);
        }
        b.setDescription(jstr);

        // Higher level description
        jstr = "";
        firstAdded = false;
        if (obj.has("higher_level")) {

            jarr = obj.getJSONArray("higher_level");
            for (int i = 0; i < jarr.length(); i++) {
                if (!firstAdded) {
                    firstAdded = true;
                } else {
                    jstr += "\n";
                }
                jstr += jarr.getString(i);
            }
        }
        b.setHigherLevelDesc(jstr);

        // School
        jso = obj.getJSONObject("school");
        String sname = jso.getString("name");
        b.setSchool(schoolFromName(sname));

        // Classes
        ArrayList<CasterClass> classes = new ArrayList<CasterClass>();
        jarr = obj.getJSONArray("classes");
        for (int i = 0; i < jarr.length(); i++) {
            String name;
            try {
                jso = jarr.getJSONObject(i);
                name = jso.getString("name");
            } catch (JSONException e) {
                name = jarr.getString(i);
            }
            classes.add(casterFromName(name));
        }
        b.setClasses(classes);

        // Subclasses
        ArrayList<SubClass> subclasses = new ArrayList<SubClass>();
        if (obj.has("subclasses")) {
            jarr = obj.getJSONArray("subclasses");
            for (int i = 0; i < jarr.length(); i++) {
                jso = jarr.getJSONObject(i);
                String name = jso.getString("name");
                subclasses.add(subclassFromName(name));
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

        ArrayList<Spell> spells = new ArrayList<Spell>();
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


    static School schoolFromName(String name) throws Exception {
        int index = Arrays.binarySearch(Spellbook.schoolNames, name);
        if (index < 0) {
            throw new Exception("Invalid school");
        }
        return School.from(index);
    }

    static CasterClass casterFromName(String name) throws Exception {
        int index = Arrays.binarySearch(Spellbook.casterNames, name);
        if (index < 0) {
            throw new Exception("Invalid caster class");
        }
        return CasterClass.from(index);
    }

    static SubClass subclassFromName(String name) throws Exception {
        int index = Arrays.binarySearch(Spellbook.subclassNames, name);
        if (index < 0) {
            System.out.println(name);
            throw new Exception("Invalid subclass: " + name);
        }
        return SubClass.from(index);
    }

    static Sourcebook sourcebookFromName(String code) throws Exception {
        code = code.toUpperCase();
        for (int i = 0; i < Spellbook.sourcebookCodes.length; i++) {
            String bookCode = Spellbook.sourcebookCodes[i];
            if (code.equals(bookCode)) {
                return Sourcebook.from(i);
            }
        }
        throw new Exception("Invalid sourcebook code: " + code);
    }


    boolean validSchoolName(String name) {
        return Arrays.asList(Spellbook.schoolNames).contains(name);
    }

    boolean validCasterName(String name) {
        return Arrays.asList(Spellbook.casterNames).contains(name);
    }

    boolean validSubName(String name) {
        return Arrays.asList(Spellbook.subclassNames).contains(name);
    }
}
