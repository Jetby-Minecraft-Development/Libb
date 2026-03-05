package me.jetby.libb.action.impl;

import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.gui.AdvancedGui;
import me.jetby.libb.gui.item.ItemWrapper;
import me.jetby.libb.gui.parser.ParsedGui;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Refresh implements Action {

    @Override
    public void execute(@NotNull ActionContext ctx, @Nullable String line) {
        ItemWrapper wrapper = ctx.get(ItemWrapper.class);
        AdvancedGui gui = ctx.get(AdvancedGui.class);
        ParsedGui parsedGui = ctx.get(ParsedGui.class);

        if (wrapper == null || gui == null) return;

        Player player = ctx.getPlayer();
        if (player == null) return;

        wrapper.refresh(player, gui.getInventory());

        if (parsedGui != null) {
            parsedGui.applyViewRequirements(player);
        }
    }
}