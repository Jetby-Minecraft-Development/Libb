package me.jetby.libb.action.impl;

import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Sound implements Action {
    @Override
    public void execute(@NotNull ActionContext ctx, @Nullable String line) {
        Player p = ctx.getPlayer();
        if (p == null) return;
        if (line == null) return;

        line = line.replace(".", "_");

        var args = line.split(";");
        org.bukkit.Sound sound;

        try {
            if (args.length >= 1) {
                sound = org.bukkit.Sound.valueOf(args[0].toUpperCase());
            } else {
                return;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }

        float volume = 0;
        float pitch = 0;

        try {
            volume = args.length > 1 ? Float.parseFloat(args[1]) : 1;
            pitch = args.length > 2 ? Float.parseFloat(args[2]) : 1;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        p.playSound(p.getLocation(), sound, volume, pitch);

    }
}