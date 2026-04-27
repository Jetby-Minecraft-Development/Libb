package me.jetby.libb.color;

import me.jetby.libb.color.serializers.*;

import java.util.function.Supplier;

public enum SerializerType {
    MINI_MESSAGE(MiniMessageSerializer::new),
    MINIMAL(MinimalSerializer::new),
    UNIFIED(UnifiedSerializer::new),
    LEGACY_AMPERSAND(() -> new LegacySerializer('&')),
    LEGACY_SECTION(() -> new LegacySerializer('§')),
    GSON(GsonSerializer::new),
    PLAIN_TEXT(PlainTextSerializer::new);

    private final Supplier<Serializer> factory;

    SerializerType(Supplier<Serializer> factory) {
        this.factory = factory;
    }

    public Serializer create() {
        return factory.get();
    }
}
