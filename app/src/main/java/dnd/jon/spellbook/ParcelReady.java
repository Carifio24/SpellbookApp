package dnd.jon.spellbook;

import android.os.Parcel;

public interface ParcelReady {

    int describeContents();
    void writeToParcel(Parcel dest, int flags);

}
