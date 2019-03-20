package dnd.jon.spellbook;

import org.json.*;
import java.util.ArrayList;
import java.util.Arrays;

class SpellParser {
    static Spell parseSpell(JSONObject obj) throws JSONException, Exception {
        // Create the JSONObject and the spell
        Spell s = new Spell();

        // Objects to reuse
        String jStr;
        JSONObject jso;
        JSONArray jarr;

        // Set the values that need no/trivial parsing
        s.setName(obj.getString("name"));
        jStr = obj.getString("page");
        String[] locationPieces = jStr.split(" ", 0);
        int page = Integer.parseInt(locationPieces[1]);
        s.setPage(page);
        Sourcebook source = sourcebookFromName(locationPieces[0]);
        s.setSourcebook(source);
        s.setDuration(obj.getString("duration"));
        s.setRange(obj.getString("range"));
        if (obj.has("ritual")) {
            s.setRitual(Util.yn_to_bool(obj.getString("ritual")));
        } else {
            s.setRitual(false);
        }
        if (obj.getString("duration").startsWith("Up to")) {
            s.setConcentration(true);
        } else if (obj.has("concentration")) {
            s.setConcentration(Util.yn_to_bool(obj.getString("concentration")));
        } else {
            s.setConcentration(false);
        }
        s.setLevel(obj.getInt("level"));
        s.setCastingTime(obj.getString("casting_time"));

        // Material, if necessary
        if (obj.has("material")) {
            s.setMaterial(obj.getString("material"));
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
        s.setComponents(components);

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
        s.setDescription(jstr);

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
        s.setHigherLevelDesc(jstr);

        // School
        jso = obj.getJSONObject("school");
        String sname = jso.getString("name");
        s.setSchool(schoolFromName(sname));

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
        s.setClasses(classes);

        // Subclasses
        ArrayList<SubClass> subclasses = new ArrayList<SubClass>();
        if (obj.has("subclasses")) {
            jarr = obj.getJSONArray("subclasses");
            for (int i = 0; i < jarr.length(); i++) {
                jso = jarr.getJSONObject(i);
                String name = jso.getString("name");
                subclasses.add(subclassFromName(name));
            }
            s.setSubclasses(subclasses);
        }

        // Return
        return s;
    }

    static ArrayList<Spell> parseSpellList(JSONArray jarr) throws Exception {

        ArrayList<Spell> spells = new ArrayList<Spell>();

        try {
            for (int i = 0; i < jarr.length(); i++) {
                Spell nextSpell = parseSpell(jarr.getJSONObject(i));
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
