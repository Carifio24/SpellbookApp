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
    private int schoolID = School.ABJURATION.getId();
    //private List<Integer> classIDs = new ArrayList<>();
    //private List<Subclass> subclasses = new ArrayList<>();
    private long sourceID = Source.PLAYERS_HANDBOOK.getId();
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
    SpellBuilder setSchoolID(int schoolID) { this.schoolID = schoolID; return this;}
    //SpellBuilder setClassIDs(List<Integer> classIDs) { this.classIDs = classIDs; return this;}
    //SpellBuilder setSubclasses(List<Subclass> subclasses) { this.subclasses = subclasses; return this;}
    SpellBuilder setSourceID(long sourceID) { this.sourceID = sourceID; return this;}
    SpellBuilder setCreated(boolean created) { this.created = created; return this; }

    Spell build() {
        return new Spell(0, name, description, higherLevel, page, range, verbal, somatic, material, materials, ritual, duration, concentration, castingTime, level, schoolID, sourceID, created);
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
        schoolID = School.ABJURATION.getId();
        //classIDs = new ArrayList<>();
        //subclasses = new ArrayList<>();
        sourceID = Source.PLAYERS_HANDBOOK.getId();
        created = false;
    }

    Spell buildAndReset() {
        Spell spell = build();
        reset();
        return spell;
    }

}
