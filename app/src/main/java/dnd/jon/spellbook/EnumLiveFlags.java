package dnd.jon.spellbook;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.EnumMap;
import java.util.function.Function;

public class EnumLiveFlags<T extends Enum<T>> {

    private final EnumMap<T, MutableLiveData<Boolean>> flags;

    EnumLiveFlags(Class<T> type, Function<T,Boolean> initialFilter) {
        flags = new EnumMap<>(type);
        final T[] values = type.getEnumConstants();
        if (values != null) {
            for (T t : values) {
                final MutableLiveData<Boolean> b = new MutableLiveData<>();
                flags.put(t, b);
                b.setValue(initialFilter.apply(t));
            }
        }
    }

    EnumLiveFlags(Class<T> type) { this(type, (x) -> true); }

    LiveData<Boolean> getLiveFlag(T t) { return flags.get(t); }
    void setLiveFlag(T t, Boolean b) { flags.get(t).setValue(b); }

}
