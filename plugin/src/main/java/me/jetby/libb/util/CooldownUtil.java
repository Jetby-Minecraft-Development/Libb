package me.jetby.libb.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownUtil {
    private static final Map<String, Long> cooldowns = new HashMap<>();

    public static long getRemaining(UUID uuid, String key) {
        long expires = cooldowns.getOrDefault(uuid + ":" + key, 0L);
        long remaining = (expires - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    public static void set(UUID uuid, String key, int seconds) {
        cooldowns.put(uuid + ":" + key, System.currentTimeMillis() + seconds * 1000L);
    }
    public static void set(UUID uuid, String key, long l) {
        cooldowns.put(uuid + ":" + key, System.currentTimeMillis() + l);
    }
}
