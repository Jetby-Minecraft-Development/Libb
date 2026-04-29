package me.jetby.libb.color.serializers;

import me.jetby.libb.color.Serializer;
import net.kyori.adventure.text.Component;

public class PlainTextSerializer implements Serializer {

    @Override
    public Component deserialize(String input) {
        return Component.text(input);
    }
}