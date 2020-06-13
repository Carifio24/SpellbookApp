package dnd.jon.spellbook;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.util.function.BiFunction;


public interface LiveMap<K,V> {
    int size();
    boolean isEmpty();
    boolean containsKey(@Nullable K k);
    @Nullable
    LiveData<V> get(@Nullable K k);
    @Nullable V set(K k, V v);
    @Nullable V remove(@Nullable K k);
    void clear();
    void setAll(BiFunction<K,V,V> function);
}
