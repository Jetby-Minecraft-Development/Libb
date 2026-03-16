package me.jetby.libb.action.impl;

import me.jetby.libb.Libb;
import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.gui.parser.ParsedGui;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Open implements Action {

    @Override
    public void execute(@NotNull ActionContext ctx, @Nullable String line) {
        Player player = ctx.getPlayer();
        if (player == null) return;
        if (line == null) return;

        new ParsedGui(player, Libb.PARSED_GUIS.get(line)).open(player);
    }
}
