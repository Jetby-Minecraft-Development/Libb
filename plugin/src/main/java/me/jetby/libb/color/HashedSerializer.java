package me.jetby.libb.color;

import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HashedSerializer implements Serializer {
    private final Serializer serializer;
    private final Map<String, Component> cache;
    private final boolean isCache;

    @Getter
    private final SerializerType type;

    public HashedSerializer(SerializerType type) {
        this(type, false, 500);
    }
    public HashedSerializer(SerializerType type, boolean cache) {
        this(type, cache, 500);
    }

    public HashedSerializer(SerializerType type, boolean cache, int maxSize) {
        this.cache = cache
                ? Collections.synchronizedMap(
                new LinkedHashMap<>(16, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<String, Component> eldest) {
                        return size() > maxSize;
                    }
                })
                : null;
        this.type = type;
        this.isCache = cache;
        this.serializer = type.create();
    }

    public Component deserialize(String input) {
        if (isCache) {
            return cache.computeIfAbsent(input, serializer::deserialize);
        }
        return serializer.deserialize(input);
    }
    /**
     * <h6>Cache Warm-up</h6>
     * Use this method to pre-cache messages; this makes usage more efficient.
     * <br>
     * <h5>Example:</h5>
     * <pre> {@code
     * List<String> messages = config.getStringList("messages");
     *
     * Serializer.UNIFIED.cacheAll(messages);
     * }
     * </pre>
     * */
    public void cacheAll(Iterable<String> inputs) {
        inputs.forEach(s -> cache.computeIfAbsent(s, serializer::deserialize));
    }

}