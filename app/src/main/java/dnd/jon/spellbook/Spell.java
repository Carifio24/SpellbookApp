package dnd.jon.spellbook;

import android.os.Parcel;
import android.os.Parcelable;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.List;
import java.util.ArrayList;

@Entity(tableName = "spells", indices = {@Index(name = "index_spells_id", value = {"id"}, unique = true), @Index(name = "index_spells_name", value = {"name"}, unique = true)},
    foreignKeys = {@ForeignKey(entity = Source.class, parentColumns = "id", childColumns = "source_id"), @ForeignKey(entity = School.class, parentColumns = "id", childColumns = "school_id")}
)
public class Spell implements Parcelable {

    // A key for database indexing
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") private final long id;

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
    @ColumnInfo(name = "school_id") private final long schoolID;
    @ColumnInfo(name = "source_id") private final long sourceID;
    @ColumnInfo(name = "created") private final boolean created;


    // Getters
    // No setters - once created, spells are immutable
    public final long getId() { return id; }
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
    public final long getSchoolID() { return schoolID; }
    //public final List<Integer> getClassIDs() { return classIDs; }
    //public final List<Subclass> getSubclasses() { return subclasses; }
    public final long getSourceID() { return sourceID; }
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
        parcel.writeLong(id);
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
        parcel.writeLong(schoolID);
        parcel.writeLong(sourceID);

        // Classes and subclasses
//        for (int j = 0; j < classIDs.size(); j++) {
//            parcel.writeInt(classIDs.get(i));
//        }
//        parcel.writeInt(-1);

//        if (subclasses != null) {
//            for (int j = 0; j < subclasses.size(); j++) {
//                parcel.writeInt(subclasses.get(j).getValue());
//            }
//        }
        //parcel.writeInt(-1);

        parcel.writeInt(created ? 1 : 0);

    }

    protected Spell(Parcel in) {
        id = in.readLong();
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
        schoolID = in.readLong();
        sourceID = in.readLong();

//        classIDs = new ArrayList<>();
//        classIDs.addAll(classInts);
//
//        subclasses = new ArrayList<>();
//        for (int i = 0; i < subclassInts.size(); i++) {
//            subclasses.add(Subclass.fromValue(subclassInts.get(i)));
//        }
        created = (in.readInt() == 1);
    }

    Spell(long id, String name, String description, String higherLevel, int page, Range range, boolean verbal, boolean somatic, boolean material, String materials,
          boolean ritual, Duration duration, boolean concentration, CastingTime castingTime,
          int level, long schoolID, long sourceID, boolean created) {
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
        this.schoolID = schoolID;
        //this.classIDs = classIDs;
        //this.subclasses = subclasses;
        this.sourceID = sourceID;
        this.created = created;
    }

    @Ignore
    protected Spell() {
        this(0, "", "", "", 0, new Range(), false, false, false, "", false, new Duration(), false, new CastingTime(), 0, School.ABJURATION.getId(), Source.PLAYERS_HANDBOOK.getId(), false);
    }

    public boolean equals(Spell other) {
        return name.equals(other.getName());
    }

}
