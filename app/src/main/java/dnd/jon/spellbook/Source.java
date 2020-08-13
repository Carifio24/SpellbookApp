package dnd.jon.spellbook;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = SpellbookRoomDatabase.SOURCES_TABLE, indices = {@Index(name = "index_sourcebooks_code", value = {"code"}, unique = true), @Index(name = "index_sourcebooks_name", value = {"name"}, unique = true)})
public class Source implements Named, Parcelable {

    // Member values
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") private final int id;

    @NonNull @ColumnInfo(name = "name") final private String name;
    @ColumnInfo(name = "code") final private String code;
    @ColumnInfo(name = "created") final private boolean created;

    static final Source PLAYERS_HANDBOOK =  new Source(1, "Player's Handbook", "PHB", false);
    static final Source XANATHARS_GTE = new Source(2, "Xanathar's Guide to Everything", "XGE", false);
    static final Source SWORD_COAST_AG = new Source(3, "Sword Coast Adv. Guide", "SCAG", false);
    static final Source LOST_LABORATORY_OK = new Source(4,"Lost Laboratory of Kwalish", "LLK", false);
    static final Source ACQUISITIONS_INCORPORATED = new Source(5, "Acquisitions Incorporated", "AI", false);

    static final Source[] BUILTIN_VALUES = { PLAYERS_HANDBOOK, XANATHARS_GTE, SWORD_COAST_AG, LOST_LABORATORY_OK, ACQUISITIONS_INCORPORATED };


    // Constructors
    // For built-in values
    Source(int id, @NonNull String name, String code, boolean created) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.created = created;
    }

    // For using in a Parcel
    protected Source(Parcel p) {
        id = p.readInt();
        final String pName = p.readString();
        name = (pName != null) ? pName : "";
        code = p.readString();
        created = p.readInt() == 1;
    }


    public static final Creator<Source> CREATOR = new Creator<Source>() {
        @Override
        public Source createFromParcel(Parcel in) {
            return new Source(in);
        }

        @Override
        public Source[] newArray(int size) {
            return new Source[size];
        }
    };

    // Getters
    public final int getId() { return id; }
    @NonNull public String getName() { return name; }
    public String getCode() { return code; }
    public boolean isCreated() { return created; }

    // For Named protocol
    public String getDisplayName() { return name; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(code);
        parcel.writeInt(created ? 1 : 0);
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof Source) && ( ((Source)other).getId() == id);
    }


}
