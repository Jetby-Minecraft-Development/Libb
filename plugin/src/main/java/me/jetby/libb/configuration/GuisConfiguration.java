package me.jetby.libb.configuration;

import me.jetby.libb.Libb;
import me.jetby.libb.LibbApi;
import me.jetby.libb.action.record.ActionBlock;
import me.jetby.libb.command.CommandRegistrar;
import me.jetby.libb.gui.parser.*;
import me.jetby.libb.util.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;

import static me.jetby.libb.command.CommandRegistrar.registerCommand;

public class GuisConfiguration {

    private final Libb plugin;

    public GuisConfiguration(Libb plugin) {
        this.plugin = plugin;
    }

    public void load() {
        LibbApi.Settings.PARSED_GUIS.clear();

        File folder = new File(plugin.getDataFolder(), "menus");
        if (!folder.exists()) folder.mkdirs();

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().endsWith(".yml")) continue;
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                loadGui(config.getString("id", file.getName().replace(".yml", "")), file);
            }
        }
    }

    public void unregisterGuiCommands() {
        for (Gui gui : LibbApi.Settings.PARSED_GUIS.values()) {
            for (String cmd : gui.command()) {
                CommandRegistrar.unregisterCommand(plugin, cmd);
            }
        }
    }

    private void loadGui(String menuId, File file) {

        if (LibbApi.Settings.PARSED_GUIS.containsKey(menuId)) {
            Logger.warn(plugin, "A duplicate of " + menuId + " was found");
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
                    new ParsedGui(player, LibbApi.Settings.PARSED_GUIS.get(menuId), Libb.INSTANCE, ParserRule.of(LibbApi.Settings.CONFIG_COLORIZER)).open(player);

                    return true;
                });
            }

            LibbApi.Settings.PARSED_GUIS.put(menuId, new Gui(
                    id, title, size, command,
                    preOpenExpressions, onOpen, onClose,
                    items));

        } catch (Exception e) {
            Logger.warn(plugin, "Error trying to load menu: " + e.getMessage());
        }
    }
}
