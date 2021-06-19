package dnd.jon.spellbook;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Array;


public class ParcelCreator<T extends ParcelReady> implements Parcelable.Creator<T> {

    interface Unpacker<T> {
        T unpack(Parcel in);
    }

    private final Class<T> cls;
    private final Unpacker<T> parcelConstructor;

    ParcelCreator(Class<T> cls, Unpacker<T> parcelConstructor) {
        this.cls = cls;
        this.parcelConstructor = parcelConstructor;
    }

    @Override
    public T createFromParcel(Parcel in) {
        return parcelConstructor.unpack(in);
    }

    @Override
    public T[] newArray(int size) {
        return (T[]) Array.newInstance(cls, size);
    }


}
