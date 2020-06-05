package dnd.jon.spellbook;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnumLiveFlags<E extends Enum<E>> implements LiveMap<E,Boolean> {

    private final EnumMap<E, MutableLiveData<Boolean>> flags;

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

    EnumLiveFlags(Class<E> type) { this(type, (x) -> true); }

    // Returns a List of the values whose flags are on
    List<E> onValues() {
        return flags.entrySet().stream().filter(x -> x.getValue().getValue()).map(Map.Entry::getKey).collect(Collectors.toList());
    }

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
