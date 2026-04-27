package me.jetby.libb.action.impl;


import me.jetby.libb.Libb;
import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BroadcastMessageImpl implements Action {

    @Override
    public void execute(@NotNull ActionContext ctx, @Nullable String line) {

        if (line != null) {
            Component context = Libb.CONFIG_COLORIZER.deserialize(line);
            Audience.audience(Bukkit.getOnlinePlayers()).sendMessage(context);
        }
    }
}
