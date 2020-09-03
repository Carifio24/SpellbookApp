package dnd.jon.spellbook;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Map;
import java.util.HashMap;

@Entity(tableName = SpellbookRoomDatabase.CLASSES_TABLE, indices = {@Index(name = "index_classes_id", value = {"id"}, unique = true), @Index(name = "index_classes_name", value = {"name"}, unique = true)})
public class CasterClass implements Named, Parcelable {

    // Member values
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") private final int id;

    @NonNull @ColumnInfo(name = "name") final private String name;

    static final CasterClass BARD = new CasterClass(1, "Bard");
    static final CasterClass CLERIC = new CasterClass(2, "Cleric");


    // Getters
    public String getName() { return name; }
    public int getId() { return id; }
    public String getDisplayName() { return name; }

    CasterClass(int id, @NonNull String name) {
        this.id = id;
        this.name = name;
    }

    protected CasterClass(Parcel p) {
       id = p.readInt();
       name = p.readString();
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(name);
    }

    public static final Creator<CasterClass> CREATOR = new Creator<CasterClass>() {
        @Override
        public CasterClass createFromParcel(Parcel in) {
            return new CasterClass(in);
        }

        @Override
        public CasterClass[] newArray(int size) {
            return new CasterClass[size];
        }
    };

    @Override
    public boolean equals(Object other) {
        return (other instanceof CasterClass) && ( ((CasterClass)other).getId() == id);
    }

}
