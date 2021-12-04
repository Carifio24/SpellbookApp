package dnd.jon.spellbook;

import android.content.Context;

import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

class SpellBuilder {

    SpellBuilder(Context context, Locale locale) {
        this.context = context;
        this.collator = Collator.getInstance(locale);
        this.classComparator = (CasterClass cc1, CasterClass cc2) -> collator.compare(DisplayUtils.getDisplayName(this.context, cc1), DisplayUtils.getDisplayName(this.context, cc2));
        this.subclassComparator = (Subclass sc1, Subclass sc2) -> collator.compare(sc1.getDisplayName(), sc2.getDisplayName());
        this.classes = new TreeSet<>(classComparator);
        this.subclasses = new TreeSet<>(subclassComparator);
        this.tashasExpandedClasses = new TreeSet<>(classComparator);
        this.locations = new HashMap<>();
        this.reset();
    }

    // Use the default locale
    SpellBuilder(Context context) {
        this(context, context.getResources().getConfiguration().getLocales().get(0));
    }

    private final Comparator<CasterClass> classComparator;
    private final Comparator<Subclass> subclassComparator;

    // Member values
    private final Context context;
    private final Collator collator;

    // Member values for spell-building
    private int id;
    private String name;
    private String description ;
    private String higherLevel;
    private Range range;
    private boolean[] components;
    private String material;
    private String royalty;
    private boolean ritual;
    private Duration duration ;
    private boolean concentration ;
    private CastingTime castingTime ;
    private int level;
    private School school;
    private SortedSet<CasterClass> classes;
    private SortedSet<Subclass> subclasses;
    private SortedSet<CasterClass> tashasExpandedClasses;
    private Map<Source,Integer> locations;

    // Setters
    SpellBuilder setID(int idIn) { id = idIn; return this; }
    SpellBuilder setName(String nameIn) {name = nameIn; return this;}
    SpellBuilder setDescription(String descriptionIn) {description = descriptionIn; return this;}
    SpellBuilder setHigherLevelDesc(String higherLevelIn) {higherLevel = higherLevelIn; return this;}
    SpellBuilder setRange(Range rangeIn) {range = rangeIn; return this;}
    SpellBuilder setComponents(boolean[] componentsIn) {components = componentsIn; return this;}
    SpellBuilder setMaterial(String materialIn) {material = materialIn; return this;}
    SpellBuilder setRoyalty(String royaltyIn) {royalty = royaltyIn; return this;}
    SpellBuilder setRitual(boolean ritualIn) {ritual = ritualIn; return this;}
    SpellBuilder setDuration(Duration durationIn) {duration = durationIn; return this;}
    SpellBuilder setConcentration(boolean concentrationIn) {concentration = concentrationIn; return this;}
    SpellBuilder setCastingTime(CastingTime castingTimeIn) {castingTime = castingTimeIn; return this;}
    SpellBuilder setLevel(int levelIn) {level = levelIn; return this;}
    SpellBuilder setSchool(School schoolIn) {school = schoolIn; return this;}
    SpellBuilder setClasses(SortedSet<CasterClass> classesIn) {classes = classesIn; return this;}
    SpellBuilder setSubclasses(SortedSet<Subclass> subclassesIn) {subclasses = subclassesIn; return this;}
    SpellBuilder setTashasExpandedClasses(SortedSet<CasterClass> tashasExpandedClassesIn) {tashasExpandedClasses = tashasExpandedClassesIn; return this;}
    SpellBuilder setLocations(Map<Source,Integer> locationsIn) {locations = locationsIn; return this;}

    SpellBuilder addClass(CasterClass cc) { classes.add(cc); return this; }
    SpellBuilder addSubclass(Subclass sc) { subclasses.add(sc); return this; }
    SpellBuilder addTashasExpandedClass(CasterClass cc) { tashasExpandedClasses.add(cc); return this; }
    SpellBuilder addLocation(Source source, Integer page) { locations.put(source, page); return this; }

    Spell build() {
        return new Spell(id, name, description, higherLevel, range, components, material, royalty, ritual, duration, concentration, castingTime, level, school, classes, subclasses, tashasExpandedClasses, locations);
    }

    void reset() {
        id = 0;
        name = "";
        description = "";
        higherLevel = "";
        range = new Range();
        components = new boolean[]{false, false, false, false};
        material = "";
        royalty = "";
        ritual = false;
        duration = new Duration();
        concentration = false;
        castingTime = new CastingTime();
        level = 0;
        school = School.ABJURATION;
        classes = new TreeSet<>(classComparator);
        subclasses = new TreeSet<>(subclassComparator);
        tashasExpandedClasses = new TreeSet<>(classComparator);
        locations = new HashMap<>();
    }

    Spell buildAndReset() {
        Spell spell = build();
        reset();
        return spell;
    }

}
