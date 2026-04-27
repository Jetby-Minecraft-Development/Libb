package me.jetby.libb.util;

import lombok.Getter;
import lombok.Setter;
import me.jetby.libb.Libb;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class VersionUtil implements Listener {

    private final String version;
    private final String lastVersion;
    private final String permission;
    private List<Component> message = new ArrayList<>();

    public VersionUtil(@NotNull JavaPlugin plugin,
                       @NotNull String version,
                       @NotNull String updateLink,
                       @NotNull String permission
    ) {
        this.version = version;
        this.lastVersion = RawUtil.getResult(updateLink);
        this.permission = permission;

        if (version.equals(lastVersion)) return;

        message.add(Libb.CONFIG_COLORIZER.deserialize(""));
        message.add(Libb.CONFIG_COLORIZER.deserialize("<yellow>" + plugin.getName() + " <gray> | <white>Attention, update available, please update the plugin."));
        message.add(Libb.CONFIG_COLORIZER.deserialize("<yellow>" + plugin.getName() + " <gray> | <white>Your version: <red>" + version + " <white>а latest <green><b>" + lastVersion));
        message.add(Libb.CONFIG_COLORIZER.deserialize(""));

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(permission)) {
            for (Component c : message) {
                player.sendMessage(c);
            }
        }
    }

}
