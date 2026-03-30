package me.jetby.libb.configuration;

import me.jetby.libb.Libb;
import me.jetby.libb.action.record.ActionBlock;
import me.jetby.libb.command.CommandRegistrar;
import me.jetby.libb.gui.parser.Gui;
import me.jetby.libb.gui.parser.Item;
import me.jetby.libb.gui.parser.ParseUtil;
import me.jetby.libb.gui.parser.ParsedGui;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;

import static me.jetby.libb.command.CommandRegistrar.registerCommand;

public class GuisConfiguration {

    private final Libb plugin;

    int amount = 0;

    public GuisConfiguration(Libb plugin) {
        this.plugin = plugin;
    }

    public void load() {
        Libb.PARSED_GUIS.clear();
        amount = 0;

        File folder = new File(plugin.getDataFolder(), "menus");
        if (!folder.exists()) folder.mkdirs();

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().endsWith(".yml")) continue;
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                loadGui(config.getString("id", file.getName().replace(".yml", "")), file);
                Bukkit.getLogger().info(file.getName() + " (id: " + config.getString("id") + ")" + " loaded");
            }
        }

    }

    public void unregisterGuiCommands() {
        try {
            Field commandMapField = plugin.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(plugin.getServer());

            for (Gui gui : Libb.PARSED_GUIS.values()) {
                for (String cmd : gui.command()) {
                    CommandRegistrar.unregisterCommand(plugin, cmd, commandMap);
                }
            }
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    Method syncCommands = plugin.getServer().getClass().getDeclaredMethod("syncCommands");
                    syncCommands.setAccessible(true);
                    syncCommands.invoke(plugin.getServer());
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to sync commands", e);
                }
            });

        } catch (Exception e) {
            plugin.getLogger().warning("Error unregistering gui commands: " + e.getMessage());
        }
    }

    private void loadGui(String menuId, File file) {

        if (Libb.PARSED_GUIS.containsKey(menuId)) {
            Bukkit.getLogger().warning("A duplicate of " + menuId + " was found");
            return;
        }
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            String id = config.getString("id");
            String title = config.getString("title");
            int size = config.getInt("size");
            List<String> command = config.getStringList("command");
            List<String> preOpenExpressions = config.getStringList("pre_open");
            ActionBlock onOpen = ParseUtil.getActionBlock(config, "on_open");
            ActionBlock onClose = ParseUtil.getActionBlock(config, "on_close");
            List<Item> items = ParseUtil.getItems(config);

            for (String cmd : command) {
                registerCommand(Libb.INSTANCE, cmd, (sender, command1, label, args) -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("The command is available only to players.");
                        return true;
                    }
                    new ParsedGui(player, Libb.PARSED_GUIS.get(menuId), Libb.INSTANCE).open(player);

                    return true;
                });
            }

            Libb.PARSED_GUIS.put(menuId, new Gui(
                    id, title, size, command,
                    preOpenExpressions, onOpen, onClose,
                    items));

            amount++;
        } catch (Exception e) {
            Bukkit.getLogger().warning("Error trying to load menu: " + e.getMessage());
        }
    }
}
