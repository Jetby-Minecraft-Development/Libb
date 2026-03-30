package me.jetby.libb.action.impl;

import me.jetby.libb.action.Action;
import me.jetby.libb.action.ActionContext;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EffectImpl implements Action {


    @Override
    public void execute(@NotNull ActionContext ctx, @Nullable String line) {
        Player player = ctx.getPlayer();
        if (player == null) return;
        if (line == null) return;

        var args = line.split(";");
        PotionEffectType potionEffectType;
        try {
            if (args.length >= 1) {
                potionEffectType = PotionEffectType.getByName(args[0].toUpperCase());
            } else {
                return;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }

        try {
            int duration = args.length > 1 ? Integer.parseInt(args[1]) : 0;
            int strength = args.length > 2 ? Integer.parseInt(args[2]) : 1;
            player.addPotionEffect(new PotionEffect(potionEffectType, duration * 20, strength));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}