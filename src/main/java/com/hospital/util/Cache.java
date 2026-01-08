package com.hospital.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple generic in-memory cache.
 */
public class Cache<K, V> {
    private final Map<K, V> map = new HashMap<>();

    public void put(K key, V value) {
        map.put(key, value);
    }

    public V get(K key) {
        return map.get(key);
    }

    public void remove(K key) {
        map.remove(key);
    }

    public void clear() {
        map.clear();
    }
}
