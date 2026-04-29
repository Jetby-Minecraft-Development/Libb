package me.jetby.libb.action.impl;

import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionInput;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ConsoleImpl implements Action {


    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput input) {
        Player player = ctx.getPlayer();
        if (player == null) return;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), input.rawText());
    }
}
