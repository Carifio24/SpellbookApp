package dnd.jon.spellbook;

import android.os.Parcel;
import android.os.Parcelable;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.List;
import java.util.ArrayList;

@Entity(tableName = "spells", indices = {@Index(value = {"name"}, unique = true)})
public class Spell implements Parcelable {

    // A key for database indexing
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") private final int id;

    // Member values
    @NonNull @ColumnInfo(name = "name") private final String name;
    @ColumnInfo(name = "description") private final String description;
    @ColumnInfo(name = "higher_level") private final String higherLevel;
    @ColumnInfo(name = "page") private final int page;
    @ColumnInfo(name = "verbal") private final boolean verbal;
    @ColumnInfo(name = "somatic") private final boolean somatic;
    @ColumnInfo(name = "material") private final boolean material;
    @ColumnInfo(name = "materials") private final String materials;
    @ColumnInfo(name = "ritual") private final boolean ritual;
    @ColumnInfo(name = "concentration") private final boolean concentration;
    @Embedded(prefix = "range_") private final Range range;
    @Embedded(prefix = "duration_") private final Duration duration;
    @Embedded(prefix = "casting_time_") private final CastingTime castingTime;
    @ColumnInfo(name = "level") private final int level;
    @ColumnInfo(name = "school") private final School school;
    @ColumnInfo(name = "sourcebook") private final Sourcebook sourcebook;
    @ColumnInfo(name = "classes") private final List<CasterClass> classes;
    @ColumnInfo(name = "subclasses") private final List<Subclass> subclasses;
    @ColumnInfo(name = "created") private final boolean created;


    // Getters
    // No setters - once created, spells are immutable
    public final int getId() { return id; }
    @NonNull public final String getName() { return name; }
    public final String getDescription() { return description; }
    public final String getHigherLevel() { return higherLevel; }
    public final int getPage() { return page; }
    public final Range getRange() { return range; }
    public final boolean hasVerbalComponent() { return verbal; }
    public final boolean hasSomaticComponent() { return somatic; }
    public final boolean hasMaterialComponent() { return material; }
    public final String getMaterials() { return materials;}
    public final boolean getRitual() { return ritual; }
    public final Duration getDuration() { return duration; }
    public final boolean getConcentration() { return concentration; }
    public final CastingTime getCastingTime() { return castingTime; }
    public final int getLevel() { return level; }
    public final School getSchool() { return school; }
    public final List<CasterClass> getClasses() { return classes; }
    public final List<Subclass> getSubclasses() { return subclasses; }
    public final Sourcebook getSourcebook() { return sourcebook; }
    public final boolean isCreated() { return created; }

    // I like the is/has naming conventions for boolean getters better
    // But Room requires 'get', so I added these as well
    public final boolean getVerbal() { return verbal; }
    public final boolean getSomatic() { return somatic; }
    public final boolean getMaterial() { return material; }

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
        if (verbal) { componentsSB.append("V"); }
        if (somatic) { componentsSB.append("S"); }
        if (material) { componentsSB.append("M"); }
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

    boolean usableBySubclass(Subclass sub) {
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
        parcel.writeInt(id);
        parcel.writeInt(page);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(higherLevel);
        parcel.writeString(range.string());
        parcel.writeInt(ritual ? 1 : 0);
        parcel.writeString(duration.string());
        parcel.writeInt(concentration ? 1 : 0);
        parcel.writeInt(verbal ? 1 : 0);
        parcel.writeInt(somatic ? 1 : 0);
        parcel.writeInt(material ? 1 : 0);
        parcel.writeString(materials);
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

        parcel.writeInt(created ? 1 : 0);

    }

    protected Spell(Parcel in) {
        id = in.readInt();
        page = in.readInt();
        final String nameStr = in.readString();
        name = (nameStr != null) ? nameStr : "";
        description = in.readString();
        higherLevel = in.readString();
        range = Range.fromString(in.readString());
        ritual = (in.readInt() == 1);
        duration = Duration.fromString(in.readString());
        concentration = (in.readInt() == 1);
        verbal = (in.readInt() == 1);
        somatic = (in.readInt() == 1);
        material = (in.readInt() == 1);
        materials = in.readString();
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
            subclasses.add(Subclass.fromValue(subclassInts.get(i)));
        }
        created = (in.readInt() == 1);
    }

    Spell(int id, String name, String description, String higherLevel, int page, Range range, boolean verbal, boolean somatic, boolean material, String materials,
          boolean ritual, Duration duration, boolean concentration, CastingTime castingTime,
          int level, School school, List<CasterClass> classes, List<Subclass> subclasses, Sourcebook sourcebook, boolean created) {
        this.id = id;
        this.name = (name != null) ? name : "";
        this.description = description;
        this.higherLevel = higherLevel;
        this.page = page;
        this.range = range;
        this.verbal = verbal;
        this.somatic = somatic;
        this.material = material;
        this.materials = materials;
        this.ritual = ritual;
        this.duration = duration;
        this.concentration = concentration;
        this.castingTime = castingTime;
        this.level = level;
        this.school = school;
        this.classes = classes;
        this.subclasses = subclasses;
        this.sourcebook = sourcebook;
        this.created = created;
    }

    @Ignore
    protected Spell() {
        this(0, "", "", "", 0, new Range(), false, false, false, "", false, new Duration(), false, new CastingTime(), 0, School.ABJURATION, new ArrayList<>(), new ArrayList<>(), Sourcebook.PLAYERS_HANDBOOK, false);
    }

    public boolean equals(Spell other) {
        return name.equals(other.getName());
    }

}
