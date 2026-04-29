package me.jetby.libb.action.impl;

import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionInput;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SoundImpl implements Action {
    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput input) {
        Player p = ctx.getPlayer();
        if (p == null) return;

        var args = input.rawText().split(";");
        if (args.length < 1) return;

        String soundName = args[0].trim().replace(".", "_").toUpperCase();
        org.bukkit.Sound sound;
        try {
            sound = org.bukkit.Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }

        float volume = 1f;
        float pitch = 1f;
        try {
            if (args.length > 1) volume = Float.parseFloat(args[1].trim());
            if (args.length > 2) pitch = Float.parseFloat(args[2].trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        p.playSound(p.getLocation(), sound, volume, pitch);
    }
}