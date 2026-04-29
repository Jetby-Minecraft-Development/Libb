package me.jetby.libb.color.serializers;

import me.jetby.libb.color.Serializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class GsonSerializer implements Serializer {
    private final GsonComponentSerializer gson = GsonComponentSerializer.gson();

    @Override
    public Component deserialize(String input) {
        return gson.deserialize(input);
    }

}