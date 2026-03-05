package me.jetby.libb.configuration;

import me.jetby.libb.Libb;
import me.jetby.libb.action.record.ActionBlock;
import me.jetby.libb.gui.parser.Gui;
import me.jetby.libb.gui.parser.Item;
import me.jetby.libb.gui.parser.ParseUtil;
import me.jetby.libb.gui.parser.ParsedGui;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenusLoader {

    private final Libb plugin;

    int amount = 0;
    public MenusLoader(Libb plugin) {
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
                Bukkit.getLogger().info(file.getName()+" (id: "+config.getString("id")+")" + " loaded");
            }
        }

    }

    private void loadGui(String menuId, File file) {

        if (Libb.PARSED_GUIS.containsKey(menuId)) {
            Bukkit.getLogger().warning("A duplicate of "+menuId+" was found");
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

            Libb.PARSED_GUIS.put(menuId, new Gui(
                    id, title, size, command,
                    preOpenExpressions, onOpen, onClose,
                    items));
            amount++;
        } catch (Exception e) {
            Bukkit.getLogger().warning("Error trying to load menu: "+e.getMessage());
        }
    }
}
