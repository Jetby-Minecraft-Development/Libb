package me.jetby.libb.action.impl;

import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionInput;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class BroadcastSoundImpl implements Action {


    @Override
    public void execute(@NotNull ActionContext ctx, @NotNull ActionInput input) {

        String line = input.rawText();

        line = line.replace(".", "_");

        var args = line.split(";");
        Sound sound;

        try {
            if (args.length >= 1) {
                sound = Sound.valueOf(args[0].toUpperCase());
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

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), sound, volume, pitch);
        }
    }
}
