package me.jetby.libb;

import me.jetby.libb.command.CommandRegistrar;
import me.jetby.libb.command.LibbCommand;
import me.jetby.libb.configuration.GuisConfiguration;
import me.jetby.libb.gui.AdvancedGui;
import me.jetby.libb.gui.GuiListener;
import me.jetby.libb.gui.parser.Gui;
import me.jetby.libb.plugin.LibbPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public final class Libb extends LibbPlugin {


    public static final Map<String, Gui> PARSED_GUIS = new HashMap<>();

    public static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static final Set<Plugin> HOOKED_PLUGINS = new HashSet<>();

    public GuisConfiguration guisConfiguration;

    public static Libb INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;

        setBStats(this, 30288);

        new LibbCommand(this).register();

        guisConfiguration = new GuisConfiguration(this);
        guisConfiguration.load();

        getServer().getPluginManager().registerEvents(new GuiListener(), this);


    }

    @Override
    public void onDisable() {
        CommandRegistrar.unregisterAll(this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory topInventory = player.getOpenInventory().getTopInventory();
            if (!(topInventory instanceof AdvancedGui)) continue;
            player.closeInventory();
        }
    }
}
