package dnd.jon.spellbook;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LiveHashMap<K,V> implements LiveMap<K,V> {

    private final HashMap<K, MutableLiveData<V>> liveMap = new HashMap<>();

    LiveHashMap(Map<K,V> map) {
        for (Map.Entry<K,V> entry : map.entrySet()) {
            final MutableLiveData<V> value = new MutableLiveData<>(entry.getValue());
            liveMap.put(entry.getKey(), value);
        }
    }

    LiveHashMap(Collection<K> items, Function<K,V> mapper) { setFrom(items, mapper);}

    LiveHashMap(K[] items, Function<K,V> mapper) { setFrom(items, mapper); }

    LiveHashMap() {}

    @Override public int size() { return liveMap.size(); }
    @Override public boolean isEmpty() { return liveMap.isEmpty(); }
    @Override public boolean containsKey(@Nullable K k) { return liveMap.containsKey(k); }
    @Nullable @Override public LiveData<V> get(@Nullable K k) { return liveMap.get(k); }
    @Override public void clear() { liveMap.clear(); }

    @Nullable @Override public V set(K k, V v) {
        if (containsKey(k)) {
            final MutableLiveData<V> data = liveMap.get(k);
            if (data == null) { return null; }
            final V vOld  = data.getValue();
            if (vOld != v) {
                data.setValue(v);
            }
            return vOld;
        } else {
            liveMap.put(k, new MutableLiveData<>(v));
            return null;
        }
    }

    @Nullable
    @Override
    public V remove(@Nullable K k) {
        final LiveData<V> data = liveMap.remove(k);
        return (data != null) ? data.getValue() : null;
    }

    public void setAll(BiFunction<K,V,V> function) {
        for (Map.Entry<K,MutableLiveData<V>> entry : liveMap.entrySet()) {
            final K key = entry.getKey();
            final MutableLiveData<V> liveValue = liveMap.get(key);
            set(key, function.apply(key, liveValue.getValue()));
        }
    }

    // Need a better name
    public void setFrom(Collection<K> items, Function<K,V> mapper) {
        for (K item : items) {
            set(item, mapper.apply(item));
        }
    }

    public void setFrom(K[] items, Function<K,V> mapper) {
        for (K item : items) {
            set(item, mapper.apply(item));
        }
    }

    public Stream<Map.Entry<K,MutableLiveData<V>>> filterEntries(BiPredicate<K,V> filter) {
        return liveMap.entrySet().stream().filter((entry) -> filter.test(entry.getKey(), entry.getValue().getValue()));
    }

    public List<K> getKeys(BiPredicate<K,V> filter) {
        return filterEntries(filter).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public List<LiveData<V>> getLiveValues(BiPredicate<K,V> filter) {
        return filterEntries(filter).map(Map.Entry::getValue).collect(Collectors.toList());
    }

    public List<V> getValues(BiPredicate<K,V> filter) {
        return filterEntries(filter).map((entry) -> entry.getValue().getValue()).collect(Collectors.toList());
    }
}
