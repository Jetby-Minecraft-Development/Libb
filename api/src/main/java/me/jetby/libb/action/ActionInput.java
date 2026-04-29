package me.jetby.libb.action;

import me.jetby.libb.LibbApi;
import me.jetby.libb.color.Serializer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ActionInput(@NotNull String rawText,
                          @Nullable Component serialized
) {

    @NotNull
    public Component getOrSerialize(@NotNull Serializer serializer) {
        if (serialized == null) {
            return serializer.deserialize(rawText);
        }
        return serialized;
    }

    @NotNull
    public Component getOrSerialize() {
        return getOrSerialize(LibbApi.Settings.CONFIG_COLORIZER);
    }

}
