package me.jetby.libb.action.impl;

import me.jetby.libb.Libb;
import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageImpl implements Action {

    @Override
    public void execute(@NotNull ActionContext ctx, @Nullable String line) {
        Player player = ctx.getPlayer();
        if (player == null) return;
        if (line == null) return;
        Component text = Libb.MINI_MESSAGE.deserialize(line);
        player.sendMessage(text);
    }
}
