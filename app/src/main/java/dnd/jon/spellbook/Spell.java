package dnd.jon.spellbook;

import android.os.Parcel;
import android.os.Parcelable;

import android.text.TextUtils;

import java.util.ArrayList;

public class Spell implements Parcelable {

    // Member values
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
    private final ArrayList<CasterClass> classes;
    private final ArrayList<SubClass> subclasses;
    private final Sourcebook sourcebook;

    // Getters
    // No setters - once created, spells are immutable
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
    public final ArrayList<CasterClass> getClasses() { return classes; }
    public final ArrayList<SubClass> getSubclasses() { return subclasses; }
    public final Sourcebook getSourcebook() { return sourcebook; }

    private String boolString(boolean b) {
        return b ? "yes" : "no";
    }

    // These methods are convenience methods, mostly for use with data binding
    public final String getLocation() { return sourcebook.getCode() + " " + page; }
    public final String getSourcebookCode() { return sourcebook.getCode(); }
    public final String getSchoolName() { return school.getDisplayName(); }
    public final String getRitualString() { return boolString(ritual); }
    public final String getConcentrationString() { return boolString(concentration); }

    // Components as a string
    public String componentsString() {
        StringBuilder componentsSB = new StringBuilder();
        if (components[0]) { componentsSB.append("V"); }
        if (components[1]) { componentsSB.append("S"); }
        if (components[2]) { componentsSB.append("M"); }
        return componentsSB.toString();
    }

    // Classes as a string
    public String classesString() {
        final String[] classStrings = new String[classes.size()];
        for (int i = 0; i < classes.size(); i++) {
            classStrings[i] = classes.get(i).getDisplayName();
        }
        return TextUtils.join(", ", classStrings);
    }

    // Get the name's hash code
    final int nameHash() { return name.hashCode(); }

    // Other member functions
    boolean usableByClass(CasterClass caster) {
        return classes.contains(caster);
    }

    boolean usableBySubclass(SubClass sub) {
        return subclasses.contains(sub);
    }

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

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(page);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(higherLevel);
        parcel.writeString(range.string());
        parcel.writeString(material);
        parcel.writeInt(ritual ? 1 : 0);
        parcel.writeString(duration.string());
        parcel.writeInt(concentration ? 1 : 0);
        parcel.writeInt(components[0] ? 1 : 0);
        parcel.writeInt(components[1] ? 1 : 0);
        parcel.writeInt(components[2] ? 1 : 0);
        parcel.writeString(castingTime.string());
        parcel.writeInt(level);
        parcel.writeInt(school.getValue());
        parcel.writeInt(sourcebook.getValue());

        // Classes and subclasses
        for (int j = 0; j < classes.size(); j++) {
            parcel.writeInt(classes.get(j).getValue());
        }
        parcel.writeInt(-1);

        if (subclasses != null) {
            for (int j = 0; j < subclasses.size(); j++) {
                parcel.writeInt(subclasses.get(j).getValue());
            }
        }
        parcel.writeInt(-1);

    }

    protected Spell(Parcel in) {
        page = in.readInt();
        name = in.readString();
        description = in.readString();
        higherLevel = in.readString();
        range = Range.fromString(in.readString());
        material = in.readString();
        ritual = (in.readInt() == 1);
        duration = Duration.fromString(in.readString());
        concentration = (in.readInt() == 1);
        components = new boolean[3];
        components[0] = (in.readInt() == 1);
        components[1] = (in.readInt() == 1);
        components[2] = (in.readInt() == 1);
        castingTime = CastingTime.fromString(in.readString());
        level = in.readInt();
        school = School.fromValue(in.readInt());
        sourcebook = Sourcebook.fromValue(in.readInt());
        int x;
        ArrayList<Integer> classInts = new ArrayList<>();
        while ((x = in.readInt()) != -1) {
            classInts.add(x);
        }
        ArrayList<Integer> subclassInts = new ArrayList<>();
        while ((x = in.readInt()) != -1) {
            subclassInts.add(x);
        }

        classes = new ArrayList<>();
        for (int i = 0; i < classInts.size(); i++) {
            classes.add(CasterClass.fromValue(classInts.get(i)));
        }

        subclasses = new ArrayList<>();
        for (int i = 0; i < subclassInts.size(); i++) {
            subclasses.add(SubClass.fromValue(subclassInts.get(i)));
        }
    }

    Spell(String nameIn, String descriptionIn, String higherLevelIn, int pageIn, Range rangeIn, boolean[] componentsIn, String materialIn,
          boolean ritualIn, Duration durationIn, boolean concentrationIn, CastingTime castingTimeIn,
          int levelIn, School schoolIn, ArrayList<CasterClass> classesIn, ArrayList<SubClass> subclassesIn, Sourcebook sourcebookIn) {
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
        sourcebook = sourcebookIn;
    }

    protected Spell() {
        this("", "", "", 0, new Range(), new boolean[]{false, false, false}, "", false, new Duration(), false, new CastingTime(), 0, School.ABJURATION, new ArrayList<>(), new ArrayList<>(), Sourcebook.PLAYERS_HANDBOOK);
    }

    public boolean equals(Spell other) {
        return name.equals(other.getName());
    }

}
