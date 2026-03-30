package me.jetby.libb.action.impl;

import me.jetby.libb.Libb;
import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BroadcastActionBarImpl implements Action {


    @Override
    public void execute(@NotNull ActionContext ctx, @Nullable String line) {
        if (line == null) return;
        Component context = Libb.MINI_MESSAGE.deserialize(line);

        Audience.audience(Bukkit.getOnlinePlayers()).sendActionBar(context);
    }
}
