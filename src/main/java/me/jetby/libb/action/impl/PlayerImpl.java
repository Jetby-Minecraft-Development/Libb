package me.jetby.libb.action.impl;


import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerImpl implements Action {

    @Override
    public void execute(@NotNull ActionContext ctx, @Nullable String line) {
        org.bukkit.entity.Player player = ctx.getPlayer();
        if (player == null) return;

        if (line != null) {
            player.chat(line);

        }
    }
}
