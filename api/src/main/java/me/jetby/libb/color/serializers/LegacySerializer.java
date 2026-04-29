package me.jetby.libb.color.serializers;

import me.jetby.libb.color.Serializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class LegacySerializer implements Serializer {
    private final LegacyComponentSerializer legacy;

    public LegacySerializer(char character) {
        this.legacy = character == '&'
                ? LegacyComponentSerializer.legacyAmpersand()
                : LegacyComponentSerializer.legacySection();
    }

    @Override
    public Component deserialize(String input) {
        return legacy.deserialize(input);
    }

}