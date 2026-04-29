package me.jetby.libb.action.impl;

import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionInput;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;


public class BroadcastTitleImpl implements Action {

    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput input) {
        var args = input.rawText().split(";");
        Component title = ctx.getSerializer().deserialize(args.length > 0 ? args[0] : "");
        Component subTitle = ctx.getSerializer().deserialize(args.length > 1 ? args[1] : "");

        Duration fadeIn = Duration.ofMillis((args.length > 2 ? Integer.parseInt(args[2]) : 10));
        Duration stayIn = Duration.ofMillis((args.length > 3 ? Integer.parseInt(args[3]) : 70));
        Duration fadeOut = Duration.ofMillis((args.length > 4 ? Integer.parseInt(args[4]) : 20));

        Audience.audience(Bukkit.getOnlinePlayers()).showTitle(Title.title(title, subTitle, Title.Times.times(fadeIn, stayIn, fadeOut)));

    }
}
