package dnd.jon.spellbook;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;
import java.util.Map;

public class LiveHashMap<K,V> implements LiveMap<K,V> {

    private final HashMap<K, MutableLiveData<V>> liveMap = new HashMap<>();

    LiveHashMap(Map<K,V> map) {
        for (Map.Entry<K,V> entry : map.entrySet()) {
            final MutableLiveData<V> value = new MutableLiveData<>(entry.getValue());
            liveMap.put(entry.getKey(), value);
        }
    }

    LiveHashMap() {}

    @Override public int size() { return liveMap.size(); }
    @Override public boolean isEmpty() { return liveMap.isEmpty(); }
    @Override public boolean containsKey(@Nullable K k) { return liveMap.containsKey(k); }
    @Nullable @Override public LiveData<V> get(@Nullable K k) { return liveMap.get(k); }
    @Override public void clear() { liveMap.clear(); }

    @Nullable @Override public V set(K k, V v) {
        if (containsKey(k)) {
            final MutableLiveData<V> data = liveMap.get(k);
            final V vOld  = data.getValue();
            data.setValue(v);
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


}
