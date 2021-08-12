package dnd.jon.spellbook;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.databinding.BaseObservable;

class SpellStatus extends BaseObservable implements Parcelable {

    boolean favorite;
    boolean prepared;
    boolean known;

    SpellStatus(boolean favorite, boolean prepared, boolean known) {
        this.favorite = favorite;
        this.prepared = prepared;
        this.known = known;
    }

    SpellStatus() {
        this(false, false, false);
    }

    protected SpellStatus(Parcel in) {
        favorite = in.readByte() != 0;
        prepared = in.readByte() != 0;
        known = in.readByte() != 0;
    }

    public static final Creator<SpellStatus> CREATOR = new Creator<SpellStatus>() {
        @Override
        public SpellStatus createFromParcel(Parcel in) {
            return new SpellStatus(in);
        }

        @Override
        public SpellStatus[] newArray(int size) {
            return new SpellStatus[size];
        }
    };

    boolean noneTrue() {
        return !(favorite || prepared || known);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (favorite ? 1 : 0));
        parcel.writeByte((byte) (prepared ? 1 : 0));
        parcel.writeByte((byte) (known ? 1 : 0));
    }
}
