package dnd.jon.spellbook;

import java.util.List;
import java.util.ArrayList;

class SpellBuilder {

    SpellBuilder() { }

    // Member values
    private String name = "";
    private String description = "";
    private String higherLevel = "";
    private int page = 0;
    private Range range = new Range();
    private boolean verbal = false;
    private boolean somatic = false;
    private boolean material = false;
    private String materials = "";
    private boolean ritual = false;
    private Duration duration = new Duration();
    private boolean concentration = false;
    private CastingTime castingTime = new CastingTime();
    private int level = 0;
    private School school = School.ABJURATION;
    private List<Integer> classIDs = new ArrayList<>();
    private List<Subclass> subclasses = new ArrayList<>();
    private int sourceID = Source.PLAYERS_HANDBOOK.getId();
    private boolean created = false;

    // Setters
    SpellBuilder setName(String name) { this.name = name; return this;}
    SpellBuilder setDescription(String description) { this.description = description; return this;}
    SpellBuilder setHigherLevelDesc(String higherLevel) { this.higherLevel = higherLevel; return this;}
    SpellBuilder setPage(int page) { this.page = page; return this;}
    SpellBuilder setRange(Range range) { this.range = range; return this;}
    SpellBuilder setVerbalComponent(boolean verbal) { this.verbal = verbal; return this; }
    SpellBuilder setSomaticComponent(boolean somatic) { this.somatic = somatic; return this; }
    SpellBuilder setMaterialComponent(boolean material) { this.material = material; return this; }
    SpellBuilder setMaterials(String materials) { this.materials = materials; return this;}
    SpellBuilder setRitual(boolean ritual) { this.ritual = ritual; return this;}
    SpellBuilder setDuration(Duration duration) { this.duration = duration; return this;}
    SpellBuilder setConcentration(boolean concentration) { this.concentration = concentration; return this;}
    SpellBuilder setCastingTime(CastingTime castingTime) { this.castingTime = castingTime; return this;}
    SpellBuilder setLevel(int level) { this.level = level; return this;}
    SpellBuilder setSchool(School school) { this.school = school; return this;}
    SpellBuilder setClassIDs(List<Integer> classIDs) { this.classIDs = classIDs; return this;}
    SpellBuilder setSubclasses(List<Subclass> subclasses) { this.subclasses = subclasses; return this;}
    SpellBuilder setSourceID(int sourceID) { this.sourceID = sourceID; return this;}
    SpellBuilder setCreated(boolean created) { this.created = created; return this; }

    Spell build() {
        return new Spell(0, name, description, higherLevel, page, range, verbal, somatic, material, materials, ritual, duration, concentration, castingTime, level, school, classIDs, subclasses, sourceID, created);
    }

    void reset() {
        name = "";
        description = "";
        higherLevel = "";
        page = 0;
        range = new Range();
        verbal = false;
        somatic = false;
        material = false;
        materials = "";
        ritual = false;
        duration = new Duration();
        concentration = false;
        castingTime = new CastingTime();
        level = 0;
        school = School.ABJURATION;
        classIDs = new ArrayList<>();
        subclasses = new ArrayList<>();
        sourceID = Source.PLAYERS_HANDBOOK.getId();
        created = false;
    }

    Spell buildAndReset() {
        Spell spell = build();
        reset();
        return spell;
    }

}
