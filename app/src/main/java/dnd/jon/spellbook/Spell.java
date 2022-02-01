package dnd.jon.spellbook;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Spell implements Parcelable {

    // Member values
    private final int id;
    private final String name;
    private final String description;
    private final String higherLevel;
    private final Range range;
    private final boolean[] components;
    private final String material;
    private final String royalty;
    private final boolean ritual;
    private final Duration duration;
    private final boolean concentration;
    private final CastingTime castingTime;
    private final int level;
    private final School school;
    private final Map<Sourcebook, Integer> locations;
    private final SortedSet<CasterClass> classes;
    private final SortedSet<Subclass> subclasses;
    private final SortedSet<CasterClass> tashasExpandedClasses;

    // Getters
    // No setters - once created, spells are immutable
    public final int getID() { return id; }
    public final String getName() { return name; }
    public final String getDescription() { return description; }
    public final String getHigherLevel() { return higherLevel; }
    public final Range getRange() { return range; }
    public final boolean[] getComponents() { return components; }
    public final String getMaterial() { return material;}
    public final String getRoyalty() { return royalty; }
    public final boolean getRitual() { return ritual; }
    public final Duration getDuration() { return duration; }
    public final boolean getConcentration() { return concentration; }
    public final CastingTime getCastingTime() { return castingTime; }
    public final int getLevel() { return level; }
    public final School getSchool() { return school; }
    public final Collection<CasterClass> getClasses() { return classes; }
    public final Collection<Subclass> getSubclasses() { return subclasses; }
    public final Collection<CasterClass> getTashasExpandedClasses() { return tashasExpandedClasses; }

    public final Map<Sourcebook, Integer> getLocations() { return locations; }
    public final int getPage(Sourcebook sourcebook) {
        Integer page = locations.get(sourcebook);
        return (page != null) ? page : 0;
    }
    public final Set<Sourcebook> getSourcebooks() { return locations.keySet(); }
    boolean inSourcebook(Sourcebook sourcebook) { return locations.containsKey(sourcebook); }

    // How many locations does the spell have listed?
    public int numberOfLocations() { return locations.size(); }

    // These methods are convenience methods, mostly for use with data binding
    public final int getSchoolNameID() { return school.getDisplayNameID(); }

    // Components as a string
    public String componentsString() {
        StringBuilder componentsSB = new StringBuilder();
        if (components[0]) { componentsSB.append("V"); }
        if (components[1]) { componentsSB.append("S"); }
        if (components[2]) { componentsSB.append("M"); }
        if (components[3]) { componentsSB.append("R"); }
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
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(higherLevel);
        parcel.writeParcelable(range, 0);
        //System.out.println("Parceling spell:");
        //System.out.println(range.internalString());
        parcel.writeString(material);
        parcel.writeString(royalty);
        parcel.writeInt(ritual ? 1 : 0);
        parcel.writeParcelable(duration, 0);
        //System.out.println(duration.internalString());
        parcel.writeInt(concentration ? 1 : 0);
        parcel.writeInt(components[0] ? 1 : 0);
        parcel.writeInt(components[1] ? 1 : 0);
        parcel.writeInt(components[2] ? 1 : 0);
        parcel.writeInt(components[3] ? 1 : 0);
        parcel.writeParcelable(castingTime, 0);
        //System.out.println(castingTime.internalString());
        parcel.writeInt(level);
        parcel.writeInt(school.getValue());
        for (Map.Entry<Sourcebook, Integer> entry : locations.entrySet()) {
            parcel.writeInt(entry.getKey().getValue());
            parcel.writeInt(entry.getValue());
        }
        parcel.writeInt(-1);

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
        name = in.readString();
        description = in.readString();
        higherLevel = in.readString();
        range = in.readParcelable(Range.class.getClassLoader());
        material = in.readString();
        royalty = in.readString();
        ritual = (in.readInt() == 1);
        duration = in.readParcelable(Duration.class.getClassLoader());
        concentration = (in.readInt() == 1);
        components = new boolean[4];
        components[0] = (in.readInt() == 1);
        components[1] = (in.readInt() == 1);
        components[2] = (in.readInt() == 1);
        components[3] = (in.readInt() == 1);
        castingTime = in.readParcelable(CastingTime.class.getClassLoader());
        level = in.readInt();
        school = School.fromValue(in.readInt());

        int x;
        Sourcebook sb;
        locations = new HashMap<>();
        while ((x = in.readInt()) != -1) {
            sb = Sourcebook.fromValue(x);
            x = in.readInt();
            locations.put(sb, x);
        }

        classes = new TreeSet<>();
        while ((x = in.readInt()) != -1) {
            classes.add(CasterClass.fromValue(x));
        }

        subclasses = new TreeSet<>();
        while ((x = in.readInt()) != -1) {
            subclasses.add(Subclass.fromValue(x));
        }

        tashasExpandedClasses = new TreeSet<>();
        while ((x = in.readInt()) != -1) {
            tashasExpandedClasses.add(CasterClass.fromValue(x));
        }

    }

    Spell(int idIn, String nameIn, String descriptionIn, String higherLevelIn, Range rangeIn, boolean[] componentsIn, String materialIn, String royaltyIn,
          boolean ritualIn, Duration durationIn, boolean concentrationIn, CastingTime castingTimeIn,
          int levelIn, School schoolIn, SortedSet<CasterClass> classesIn, SortedSet<Subclass> subclassesIn, SortedSet<CasterClass> tashasExpandedClassesIn, Map<Sourcebook,Integer> locationsIn) {
        id = idIn;
        name = nameIn;
        description = descriptionIn;
        higherLevel = higherLevelIn;
        range = rangeIn;
        components = componentsIn;
        material = materialIn;
        royalty = royaltyIn;
        ritual = ritualIn;
        duration = durationIn;
        concentration = concentrationIn;
        castingTime = castingTimeIn;
        level = levelIn;
        school = schoolIn;
        classes = classesIn;
        subclasses = subclassesIn;
        tashasExpandedClasses = tashasExpandedClassesIn;
        locations = locationsIn;
    }

    protected Spell() {
        this(0, "", "", "", new Range(), new boolean[]{false, false, false, false}, "", "", false, new Duration(), false, new CastingTime(), 0, School.ABJURATION, new TreeSet<>(), new TreeSet<>(), new TreeSet<>(), new HashMap<>());
    }

    public boolean equals(Spell other) {
        return name.equals(other.getName());
    }

}
