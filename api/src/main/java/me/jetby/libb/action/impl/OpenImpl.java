package me.jetby.libb.action.impl;

import me.jetby.libb.LibbApi;
import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionInput;
import me.jetby.libb.gui.parser.ParsedGui;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OpenImpl implements Action {

    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput input) {
        Player player = ctx.getPlayer();
        if (player == null) return;

        new ParsedGui(player, LibbApi.Settings.PARSED_GUIS.get(input.rawText()), ctx.getPlugin()).open(player);
    }
}
