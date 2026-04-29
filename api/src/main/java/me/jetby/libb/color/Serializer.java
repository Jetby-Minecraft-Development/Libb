package me.jetby.libb.color;

import net.kyori.adventure.text.Component;

public interface Serializer {
    Component deserialize(String input);

    static HashedSerializer get(SerializerType type) {
        return switch (type) {
            case GSON -> GSON;
            case MINI_MESSAGE -> MINI_MESSAGE;
            case MINIMAL -> MINIMAL;
            case UNIFIED -> UNIFIED;
            case PLAIN_TEXT -> PLAIN_TEXT;
            case LEGACY_SECTION -> LEGACY_SECTION;
            case LEGACY_AMPERSAND -> LEGACY_AMPERSAND;
        };
    }



    HashedSerializer PLAIN_TEXT = new HashedSerializer(SerializerType.PLAIN_TEXT);
    HashedSerializer GSON = new HashedSerializer(SerializerType.GSON, true);
    HashedSerializer LEGACY_SECTION = new HashedSerializer(SerializerType.LEGACY_SECTION, true);
    HashedSerializer LEGACY_AMPERSAND = new HashedSerializer(SerializerType.LEGACY_AMPERSAND, true);
    HashedSerializer UNIFIED = new HashedSerializer(SerializerType.UNIFIED, true);
    HashedSerializer MINIMAL = new HashedSerializer(SerializerType.MINIMAL, true);
    HashedSerializer MINI_MESSAGE = new HashedSerializer(SerializerType.MINI_MESSAGE, true);
}
