package dnd.jon.spellbook;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EnumLiveFlags<E extends Enum<E>> implements LiveMap<E,Boolean> {

    private final EnumMap<E, MutableLiveData<Boolean>> flags;

    // Create a new instance by specifying the enum type and a filter that determines which flags are initially set
    EnumLiveFlags(Class<E> type, Function<E,Boolean> initialFilter) {
        flags = new EnumMap<>(type);
        final E[] values = type.getEnumConstants();
        if (values != null) {
            for (E e : values) {
                final MutableLiveData<Boolean> b = new MutableLiveData<>();
                flags.put(e, b);
                b.setValue(initialFilter.apply(e));
            }
        }
    }

    // Instead of providing a filter, one can set the initial state to all true/false by supplying a boolean
    EnumLiveFlags(Class<E> type, Boolean b) { this(type, (x) -> b); }

    // If no filter is provided, set all flags to true
    EnumLiveFlags(Class<E> type) { this(type, (x) -> true); }

    // Return a list of keys whose values satisfy a certain predicate
    private List<E> getKeys(Predicate<Map.Entry<E,MutableLiveData<Boolean>>> predicate) {
        return flags.entrySet().stream().filter(predicate).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    // Get the Boolean value of a LiveData<Boolean>, with null checks
    private Boolean liveValue(LiveData<Boolean> liveData) {
        if (liveData == null) { return false; }
        final Boolean value = liveData.getValue();
        return (value != null) ? value : false;
    }

    // Return the lists of values whose flags are set to on and off, respectively
    List<E> onValues() { return getKeys((e) -> liveValue(e.getValue())); }
    List<E> offValues() { return getKeys((e) -> !liveValue(e.getValue())); }

    // LiveMap methods
    @Override public int size() { return flags.size(); }
    @Override public boolean isEmpty() { return flags.isEmpty(); }
    @Override public boolean containsKey(@Nullable E e) { return flags.containsKey(e); }
    @Nullable @Override public LiveData<Boolean> get(@Nullable E e) { return flags.get(e); }
    @Override public void clear() { flags.clear(); }

    @Nullable
    @Override
    public Boolean set(E e, Boolean b) {
        if (containsKey(e)) {
            final MutableLiveData<Boolean> data = flags.get(e);
            if (data == null) { return null; }
            final Boolean bOld = data.getValue();
            if (bOld != b) {
                data.setValue(b);
            }
            return bOld;
        } else {
            flags.put(e, new MutableLiveData<>(b));
            return null;
        }
    }

    @Nullable @Override public Boolean remove(@Nullable E e) {
        final LiveData<Boolean> data = flags.remove(e);
        return (data != null) ? data.getValue() : null;
    }
}
