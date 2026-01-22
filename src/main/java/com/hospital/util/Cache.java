package com.hospital.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced in-memory cache with TTL (Time To Live) support for database query results.
 */
public class Cache<K, V> {
    private final Map<K, CacheEntry<V>> map = new ConcurrentHashMap<>();
    private final long defaultTtlMinutes;

    public Cache() {
        this.defaultTtlMinutes = 30; // Default 30 minutes TTL
    }

    public Cache(long defaultTtlMinutes) {
        this.defaultTtlMinutes = defaultTtlMinutes;
    }

    private static class CacheEntry<V> {
        private final V value;
        private final LocalDateTime expiryTime;

        public CacheEntry(V value, long ttlMinutes) {
            this.value = value;
            this.expiryTime = LocalDateTime.now().plusMinutes(ttlMinutes);
        }

        public V getValue() {
            return value;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }

    public void put(K key, V value) {
        put(key, value, defaultTtlMinutes);
    }

    public void put(K key, V value, long ttlMinutes) {
        map.put(key, new CacheEntry<>(value, ttlMinutes));
    }

    public V get(K key) {
        CacheEntry<V> entry = map.get(key);
        if (entry != null && !entry.isExpired()) {
            return entry.getValue();
        } else if (entry != null && entry.isExpired()) {
            map.remove(key); // Clean up expired entry
        }
        return null;
    }

    public void remove(K key) {
        map.remove(key);
    }

    public void clear() {
        map.clear();
    }

    public boolean containsKey(K key) {
        CacheEntry<V> entry = map.get(key);
        return entry != null && !entry.isExpired();
    }

    public int size() {
        // Clean up expired entries and count valid ones
        map.entrySet().removeIf(entry -> entry.getValue().isExpired());
        return map.size();
    }

    /**
     * Gets cache statistics.
     */
    public CacheStats getStats() {
        int totalEntries = map.size();
        int expiredEntries = 0;
        long totalAccessTime = 0;

        for (CacheEntry<V> entry : map.values()) {
            if (entry.isExpired()) {
                expiredEntries++;
            }
        }

        return new CacheStats(totalEntries, expiredEntries, totalEntries - expiredEntries);
    }

    public static class CacheStats {
        private final int totalEntries;
        private final int expiredEntries;
        private final int activeEntries;

        public CacheStats(int totalEntries, int expiredEntries, int activeEntries) {
            this.totalEntries = totalEntries;
            this.expiredEntries = expiredEntries;
            this.activeEntries = activeEntries;
        }

        public int getTotalEntries() { return totalEntries; }
        public int getExpiredEntries() { return expiredEntries; }
        public int getActiveEntries() { return activeEntries; }
        public double getHitRate() { return totalEntries > 0 ? (double) activeEntries / totalEntries : 0.0; }
    }
}
