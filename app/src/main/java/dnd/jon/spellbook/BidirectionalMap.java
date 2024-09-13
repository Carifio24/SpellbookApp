package dnd.jon.spellbook;

import java.util.HashMap;
import java.util.Map;

// Adapted from https://stackoverflow.com/a/74828056

interface BidirectionalMap<K, V> {
    void put(K key, V value);
    V getValue(K key);
    K getKey(V value);
    void removeByKey(K key);
    void removeByValue(V value);
    void removeIfMatch(K key, V value);
    boolean linkExists(K key, V value);
}

class BidirectionalHashMap<K, V> implements BidirectionalMap<K, V> {
    final private Map<K, V> valueByKey = new HashMap<>();
    final private Map<V, K> keyByValue = new HashMap<>();

    public void put(K key, V value) {
        valueByKey.put(key, value);
        keyByValue.put(value, key);
    }

    public V getValue(K key) {
        return valueByKey.get(key);
    }

    public K getKey(V value) {
        return keyByValue.get(value);
    }

    public void removeByKey(K key) {
        final V toRemove = valueByKey.remove(key);
        keyByValue.remove(toRemove);
    }

    public void removeByValue(V value) {
        final K toRemove = keyByValue.remove(value);
        valueByKey.remove(toRemove);
    }

    public void removeIfMatch(K key, V value) { // removes entries only if the given association key/value is correct
        valueByKey.remove(key, value);
        keyByValue.remove(value, key);
    }

    public boolean linkExists(K key, V value) {
        return getValue(key).equals(value) || getKey(value).equals(key);
    }

}