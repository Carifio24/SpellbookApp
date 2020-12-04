package dnd.jon.spellbook;

import android.content.Context;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

class SpellBuilder {

    private final Context context;
    private final Collator collator;

    SpellBuilder(Context context, Locale locale) {
        this.context = context;
        this.collator = Collator.getInstance(locale);
        classComparator = (CasterClass cc1, CasterClass cc2) -> collator.compare(DisplayUtils.getDisplayName(this.context, cc1), DisplayUtils.getDisplayName(this.context, cc2));
        subclassComparator = (Subclass sc1, Subclass sc2) -> collator.compare(sc1.getDisplayName(), sc2.getDisplayName());
        classes = new TreeSet<>(classComparator);
        subclasses = new TreeSet<>(subclassComparator);
        tashasExpandedClasses = new TreeSet<>(classComparator);
    }

    // Use the default locale
    SpellBuilder(Context context) {
        this(context, context.getResources().getConfiguration().getLocales().get(0));
    }

    private final Comparator<CasterClass> classComparator;
    private final Comparator<Subclass> subclassComparator;

    // Member values
    private int id = 0;
    private String name = "";
    private String description = "";
    private String higherLevel = "";
    private int page = 0;
    private Range range = new Range();
    private boolean[] components = {false, false, false};
    private String material = "";
    private boolean ritual = false;
    private Duration duration = new Duration();
    private boolean concentration = false;
    private CastingTime castingTime = new CastingTime();
    private int level = 0;
    private School school = School.ABJURATION;
    private SortedSet<CasterClass> classes;
    private SortedSet<Subclass> subclasses;
    private SortedSet<CasterClass> tashasExpandedClasses;
    private Sourcebook sourcebook = Sourcebook.PLAYERS_HANDBOOK;

    // Setters
    SpellBuilder setID(int idIn) { id = idIn; return this; }
    SpellBuilder setName(String nameIn) {name = nameIn; return this;}
    SpellBuilder setDescription(String descriptionIn) {description = descriptionIn; return this;}
    SpellBuilder setHigherLevelDesc(String higherLevelIn) {higherLevel = higherLevelIn; return this;}
    SpellBuilder setPage(int pageIn) {page = pageIn; return this;}
    SpellBuilder setRange(Range rangeIn) {range = rangeIn; return this;}
    SpellBuilder setComponents(boolean[] componentsIn) {components = componentsIn; return this;}
    SpellBuilder setMaterial(String materialIn) {material = materialIn; return this;}
    SpellBuilder setRitual(boolean ritualIn) {ritual = ritualIn; return this;}
    SpellBuilder setDuration(Duration durationIn) {duration = durationIn; return this;}
    SpellBuilder setConcentration(boolean concentrationIn) {concentration = concentrationIn; return this;}
    SpellBuilder setCastingTime(CastingTime castingTimeIn) {castingTime = castingTimeIn; return this;}
    SpellBuilder setLevel(int levelIn) {level = levelIn; return this;}
    SpellBuilder setSchool(School schoolIn) {school = schoolIn; return this;}
    SpellBuilder setClasses(SortedSet<CasterClass> classesIn) {classes = classesIn; return this;}
    SpellBuilder setSubclasses(SortedSet<Subclass> subclassesIn) {subclasses = subclassesIn; return this;}
    SpellBuilder setTashasExpandedClasses(SortedSet<CasterClass> tashasExpandedClassesIn) {tashasExpandedClasses = tashasExpandedClassesIn; return this;}
    SpellBuilder setSourcebook(Sourcebook sourcebookIn) {sourcebook = sourcebookIn; return this;}

    SpellBuilder addClass(CasterClass cc) { classes.add(cc); return this; }
    SpellBuilder addSubclass(Subclass sc) { subclasses.add(sc); return this; }
    SpellBuilder addTashasExpandedClass(CasterClass cc) { tashasExpandedClasses.add(cc); return this; }

    Spell build() {
        return new Spell(id, name, description, higherLevel, page, range, components, material, ritual, duration, concentration, castingTime, level, school, classes, subclasses, tashasExpandedClasses, sourcebook);
    }

    void reset() {
        id = 0;
        name = "";
        description = "";
        higherLevel = "";
        page = 0;
        range = new Range();
        components = new boolean[]{false, false, false};
        material = "";
        ritual = false;
        duration = new Duration();
        concentration = false;
        castingTime = new CastingTime();
        level = 0;
        school = School.ABJURATION;
        classes = new TreeSet<>(classComparator);
        subclasses = new TreeSet<>(subclassComparator);
        tashasExpandedClasses = new TreeSet<>(classComparator);
        sourcebook = Sourcebook.PLAYERS_HANDBOOK;
    }

    Spell buildAndReset() {
        Spell spell = build();
        reset();
        return spell;
    }

}
