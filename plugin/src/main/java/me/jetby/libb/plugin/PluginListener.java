package me.jetby.libb.plugin;

import me.jetby.libb.Libb;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

public class PluginListener implements Listener {
    public PluginListener() {
        Bukkit.getPluginManager().registerEvents(this, Libb.INSTANCE);
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        Plugin plugin = e.getPlugin();
        if (!Libb.HOOKED_PLUGINS.contains(plugin)) return;
        Libb.HOOKED_PLUGINS.remove(plugin);
    }
}
