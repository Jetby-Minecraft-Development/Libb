package me.jetby.libb.action.impl;

import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionInput;
import me.jetby.libb.gui.parser.ParsedGui;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RefreshImpl implements Action {

    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput input) {
        ParsedGui parsedGui = ctx.get(ParsedGui.class);

        Player player = ctx.getPlayer();
        if (player == null) return;

        if (parsedGui == null) return;
        parsedGui.refresh();
    }
}