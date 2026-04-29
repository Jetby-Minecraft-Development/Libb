package me.jetby.libb.action.impl;


import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionInput;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class BroadcastMessageImpl implements Action {

    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput input) {

        Audience.audience(Bukkit.getOnlinePlayers()).sendMessage(input.getOrSerialize());

    }
}
