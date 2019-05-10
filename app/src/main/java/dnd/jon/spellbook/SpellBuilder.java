package dnd.jon.spellbook;

import java.util.ArrayList;

class SpellBuilder {

    SpellBuilder() { }

    // Member values
    private String name = "";
    private String description = "";
    private String higherLevel = "";
    private int page = 0;
    private String range = "";
    private boolean[] components = {false, false, false};
    private String material = "";
    private boolean ritual = false;
    private String duration = "";
    private boolean concentration = false;
    private String castingTime = "";
    private int level = 0;
    private School school = School.ABJURATION;
    private ArrayList<CasterClass> classes = new ArrayList<>();
    private ArrayList<SubClass> subclasses = new ArrayList<>();
    private Sourcebook sourcebook = Sourcebook.PLAYERS_HANDBOOK;

    // Setters
    SpellBuilder setName(String nameIn) {name = nameIn; return this;}
    SpellBuilder setDescription(String descriptionIn) {description = descriptionIn; return this;}
    SpellBuilder setHigherLevelDesc(String higherLevelIn) {higherLevel = higherLevelIn; return this;}
    SpellBuilder setPage(int pageIn) {page = pageIn; return this;}
    SpellBuilder setRange(String rangeIn) {range = rangeIn; return this;}
    SpellBuilder setComponents(boolean[] componentsIn) {components = componentsIn; return this;}
    SpellBuilder setMaterial(String materialIn) {material = materialIn; return this;}
    SpellBuilder setRitual(boolean ritualIn) {ritual = ritualIn; return this;}
    SpellBuilder setDuration(String durationIn) {duration = durationIn; return this;}
    SpellBuilder setConcentration(boolean concentrationIn) {concentration = concentrationIn; return this;}
    SpellBuilder setCastingTime(String castingTimeIn) {castingTime = castingTimeIn; return this;}
    SpellBuilder setLevel(int levelIn) {level = levelIn; return this;}
    SpellBuilder setSchool(School schoolIn) {school = schoolIn; return this;}
    SpellBuilder setClasses(ArrayList<CasterClass> classesIn) {classes = classesIn; return this;}
    SpellBuilder setSubclasses(ArrayList<SubClass> subclassesIn) {subclasses = subclassesIn; return this;}
    SpellBuilder setSourcebook(Sourcebook sourcebookIn) {sourcebook = sourcebookIn; return this;}

    Spell build() {
        return new Spell(name, description, higherLevel, page, range, components, material, ritual, duration, concentration, castingTime, level, school, classes, subclasses, sourcebook);
    }

}
