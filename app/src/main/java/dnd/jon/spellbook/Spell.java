package dnd.jon.spellbook;

import android.os.Parcel;
import android.os.Parcelable;

import android.text.TextUtils;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

public class Spell implements Parcelable {

    // Member values
    private final int id;
    private final String name;
    private final String description;
    private final String higherLevel;
    private final int page;
    private final Range range;
    private final boolean[] components;
    private final String material;
    private final boolean ritual;
    private final Duration duration;
    private final boolean concentration;
    private final CastingTime castingTime;
    private final int level;
    private final School school;
    private final Sourcebook sourcebook;
    private final SortedSet<CasterClass> classes;
    private final SortedSet<Subclass> subclasses;
    private final SortedSet<CasterClass> tashasExpandedClasses;

    // Getters
    // No setters - once created, spells are immutable
    public final int getID() { return id; }
    public final String getName() { return name; }
    public final String getDescription() { return description; }
    public final String getHigherLevel() { return higherLevel; }
    public final int getPage() { return page; }
    public final Range getRange() { return range; }
    public final boolean[] getComponents() { return components; }
    public final String getMaterial() { return material;}
    public final boolean getRitual() { return ritual; }
    public final Duration getDuration() { return duration; }
    public final boolean getConcentration() { return concentration; }
    public final CastingTime getCastingTime() { return castingTime; }
    public final int getLevel() { return level; }
    public final School getSchool() { return school; }
    public final Collection<CasterClass> getClasses() { return classes; }
    public final Collection<Subclass> getSubclasses() { return subclasses; }
    public final Collection<CasterClass> getTashasExpandedClasses() { return tashasExpandedClasses; }
    public final Sourcebook getSourcebook() { return sourcebook; }

    // These methods are convenience methods, mostly for use with data binding
    public final int getSchoolNameID() { return school.getDisplayNameID(); }

    // Components as a string
    public String componentsString() {
        StringBuilder componentsSB = new StringBuilder();
        if (components[0]) { componentsSB.append("V"); }
        if (components[1]) { componentsSB.append("S"); }
        if (components[2]) { componentsSB.append("M"); }
        return componentsSB.toString();
    }

    // Get the name's hash code
    final int nameHash() { return name.hashCode(); }

    // Is the spell usable by a given class? By a given subclass?
    boolean inSpellList(CasterClass caster) {
        return classes.contains(caster);
    }
    boolean inSpellList(Subclass sub) {
        return subclasses.contains(sub);
    }
    boolean inExpandedSpellList(CasterClass caster) { return tashasExpandedClasses.contains(caster); }


    //// Parcelable stuff
    public static final Creator<Spell> CREATOR = new Creator<Spell>() {
        @Override
        public Spell createFromParcel(Parcel in) {
            return new Spell(in);
        }

        @Override
        public Spell[] newArray(int size) {
            return new Spell[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    // Write a spell to a parcel
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(page);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(higherLevel);
        parcel.writeString(range.internalString());
        System.out.println("Parceling spell:");
        System.out.println(range.internalString());
        parcel.writeString(material);
        parcel.writeInt(ritual ? 1 : 0);
        parcel.writeString(duration.internalString());
        System.out.println(duration.internalString());
        parcel.writeInt(concentration ? 1 : 0);
        parcel.writeInt(components[0] ? 1 : 0);
        parcel.writeInt(components[1] ? 1 : 0);
        parcel.writeInt(components[2] ? 1 : 0);
        parcel.writeString(castingTime.internalString());
        System.out.println(castingTime.internalString());
        parcel.writeInt(level);
        parcel.writeInt(school.getValue());
        parcel.writeInt(sourcebook.getValue());

        // Classes and subclasses
        for (CasterClass cc : classes) {
            parcel.writeInt(cc.getValue());
        }
        parcel.writeInt(-1);

        if (subclasses != null) {
            for (Subclass sc : subclasses) {
                parcel.writeInt(sc.getValue());
            }
        }
        parcel.writeInt(-1);

        for (CasterClass cc : tashasExpandedClasses) {
            parcel.writeInt(cc.getValue());
        }
        parcel.writeInt(-1);

    }

    // Create a spell from a Parcel
    protected Spell(Parcel in) {
        id = in.readInt();
        page = in.readInt();
        name = in.readString();
        description = in.readString();
        higherLevel = in.readString();
        final String rangeString = in.readString();
        range = Range.fromInternalString(rangeString);
        System.out.println("rangeString: " + rangeString);
        System.out.println("Range: " + range);
        System.out.println("unit: " + range.unit);
        System.out.println("type: " + range.type);
        material = in.readString();
        ritual = (in.readInt() == 1);
        final String durationString = in.readString();
        duration = Duration.fromInternalString(durationString);
        System.out.println("durationString: " + durationString);
        System.out.println("Duration: " + duration);
        System.out.println("unit: " + duration.unit);
        System.out.println("type: " + duration.type);
        concentration = (in.readInt() == 1);
        components = new boolean[3];
        components[0] = (in.readInt() == 1);
        components[1] = (in.readInt() == 1);
        components[2] = (in.readInt() == 1);
        final String castingTimeString = in.readString();
        castingTime = CastingTime.fromInternalString(castingTimeString);
        System.out.println("castingTimeString: " + castingTimeString);
        System.out.println("Casting time: " + castingTime);
        System.out.println("unit: " + castingTime.unit);
        System.out.println("type: " + castingTime.type);
        level = in.readInt();
        school = School.fromValue(in.readInt());
        sourcebook = Sourcebook.fromValue(in.readInt());

        int x;
        List<Integer> classInts = new ArrayList<>();
        while ((x = in.readInt()) != -1) {
            classInts.add(x);
        }
        List<Integer> subclassInts = new ArrayList<>();
        while ((x = in.readInt()) != -1) {
            subclassInts.add(x);
        }
        List<Integer> expandedClassInts = new ArrayList<>();
        while ((x = in.readInt()) != -1) {
            expandedClassInts.add(x);
        }

        classes = new TreeSet<>();
        for (int i = 0; i < classInts.size(); i++) {
            classes.add(CasterClass.fromValue(classInts.get(i)));
        }

        subclasses = new TreeSet<>();
        for (int i = 0; i < subclassInts.size(); i++) {
            subclasses.add(Subclass.fromValue(subclassInts.get(i)));
        }

        tashasExpandedClasses = new TreeSet<>();
        for (int i = 0; i < expandedClassInts.size(); i++) {
            tashasExpandedClasses.add(CasterClass.fromValue(expandedClassInts.get(i)));
        }
    }

    Spell(int idIn, String nameIn, String descriptionIn, String higherLevelIn, int pageIn, Range rangeIn, boolean[] componentsIn, String materialIn,
          boolean ritualIn, Duration durationIn, boolean concentrationIn, CastingTime castingTimeIn,
          int levelIn, School schoolIn, SortedSet<CasterClass> classesIn, SortedSet<Subclass> subclassesIn, SortedSet<CasterClass> tashasExpandedClassesIn, Sourcebook sourcebookIn) {
        id = idIn;
        name = nameIn;
        description = descriptionIn;
        higherLevel = higherLevelIn;
        page = pageIn;
        range = rangeIn;
        components = componentsIn;
        material = materialIn;
        ritual = ritualIn;
        duration = durationIn;
        concentration = concentrationIn;
        castingTime = castingTimeIn;
        level = levelIn;
        school = schoolIn;
        classes = classesIn;
        subclasses = subclassesIn;
        tashasExpandedClasses = tashasExpandedClassesIn;
        sourcebook = sourcebookIn;
    }

    protected Spell() {
        this(0, "", "", "", 0, new Range(), new boolean[]{false, false, false}, "", false, new Duration(), false, new CastingTime(), 0, School.ABJURATION, new TreeSet<>(), new TreeSet<>(), new TreeSet<>(), Sourcebook.PLAYERS_HANDBOOK);
    }

    public boolean equals(Spell other) {
        return name.equals(other.getName());
    }

}
