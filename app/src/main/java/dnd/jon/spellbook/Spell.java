package dnd.jon.spellbook;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.ArrayList;

public class Spell implements Parcelable {

    // Member values
    private String name;
    private String description;
    private String higherLevel;
    private int page;
    private String range;
    private boolean[] components;
    private String material;
    private boolean ritual;
    private String duration;
    private boolean concentration;
    private String castingTime;
    private int level;
    private School school;
    private ArrayList<CasterClass> classes;
    private ArrayList<SubClass> subclasses;

    // Getters
    final String getName() {return name;}
    final String getDescription() {return description;}
    final String getHigherLevelDesc() {return higherLevel;}
    final int getPage() {return page;}
    final String getRange() {return range;}
    final boolean[] getComponents() {return components;}
    final String getMaterial() {return material;}
    final boolean getRitual() {return ritual;}
    final String getDuration() {return duration;}
    final boolean getConcentration() {return concentration;}
    final String getCastingTime() {return castingTime;}
    final int getLevel() {return level;}
    final School getSchool() {return school;}
    final ArrayList<CasterClass> getClasses() {return classes;}
    final ArrayList<SubClass> getSubclasses() {return subclasses;}

    // Setters
    void setName(String nameIn) {name = nameIn;}
    void setDescription(String descriptionIn) {description = descriptionIn;}
    void setHigherLevelDesc(String higherLevelIn) {higherLevel = higherLevelIn;}
    void setPage(int pageIn) {page = pageIn;}
    void setRange(String rangeIn) {range = rangeIn;}
    void setComponents(boolean[] componentsIn) {components = componentsIn;}
    void setMaterial(String materialIn) {material = materialIn;}
    void setRitual(boolean ritualIn) {ritual = ritualIn;}
    void setDuration(String durationIn) {duration = durationIn;}
    void setConcentration(boolean concentrationIn) {concentration = concentrationIn;}
    void setCastingTime(String castingTimeIn) {castingTime = castingTimeIn;}
    void setLevel(int levelIn) {level = levelIn;}
    void setSchool(School schoolIn) {school = schoolIn;}
    void setClasses(ArrayList<CasterClass> classesIn) {classes = classesIn;}
    void setSubclasses(ArrayList<SubClass> subclassesIn) {subclasses = subclassesIn;}

    // Get the name's hash code
    final int nameHash() {return name.hashCode();}

    // Other member functions
    boolean usableByClass(CasterClass caster) {
        for (CasterClass cc : classes) {
            if (cc == caster) {
                return true;
            }
        }
        return false;
    }

    boolean usableBySubclass(SubClass sub) {
        for (SubClass sc : subclasses) {
            if (sc == sub) {
                return true;
            }
        }
        return false;
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
        parcel.writeString(range);
        parcel.writeString(material);
        parcel.writeInt(ritual ? 1 : 0);
        parcel.writeString(duration);
        parcel.writeInt(concentration ? 1 : 0);
        parcel.writeInt(components[0] ? 1 : 0);
        parcel.writeInt(components[1] ? 1 : 0);
        parcel.writeInt(components[2] ? 1 : 0);
        parcel.writeString(castingTime);
        parcel.writeInt(level);
        parcel.writeInt(school.value);

        // Classes and subclasses
        int[] classInts = new int[classes.size()];
        for (int j = 0; j < classes.size(); j++) {
            classInts[j] = classes.get(j).value;
        }
        int[] subclassInts = new int[subclasses.size()];
        for (int j = 0; j < subclasses.size(); j++) {
            subclassInts[j] = subclasses.get(j).value;
        }
        parcel.writeIntArray(classInts);
        parcel.writeIntArray(subclassInts);

    }

    protected Spell() {

    }

    protected Spell(Parcel in) {
        page = in.readInt();
        name = in.readString();
        description = in.readString();
        higherLevel = in.readString();
        range = in.readString();
        material = in.readString();
        ritual = (in.readInt() == 1);
        duration = in.readString();
        concentration = (in.readInt() == 1);
        components = new boolean[3];
        components[0] = (in.readInt() == 1);
        components[1] = (in.readInt() == 1);
        components[2] = (in.readInt() == 1);
        castingTime = in.readString();
        level = in.readInt();
        school = School.from(in.readInt());
        int[] classInts = new int[Spellbook.N_CASTERS];
        in.readIntArray(classInts);
        int[] subclassInts = new int[Spellbook.N_SUBCLASSES];
        in.readIntArray(subclassInts);

        classes = new ArrayList<CasterClass>();
        for (int i = 0; i < Spellbook.N_CASTERS; i++) {
            classes.add(CasterClass.from(classInts[i]));
        }

        subclasses = new ArrayList<SubClass>();
        for (int i = 0; i < Spellbook.N_SUBCLASSES; i++) {
            subclasses.add(SubClass.from(subclassInts[i]));
        }


    }
}
