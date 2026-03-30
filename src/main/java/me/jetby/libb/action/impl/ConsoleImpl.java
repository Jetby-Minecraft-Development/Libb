package me.jetby.libb.action.impl;

import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConsoleImpl implements Action {


    @Override
    public void execute(@NotNull ActionContext ctx, @Nullable String line) {
        Player player = ctx.getPlayer();
        if (player == null) return;
        if (line == null) return;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), line);
    }
}
