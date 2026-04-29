package me.jetby.libb;

import me.jetby.libb.command.AdvancedCommand;
import me.jetby.libb.command.annotations.Permission;
import me.jetby.libb.command.annotations.SubCommand;
import me.jetby.libb.command.annotations.TabComplete;
import me.jetby.libb.command.annotations.messages.InsufficientArgs;
import me.jetby.libb.gui.AdvancedGui;
import me.jetby.libb.gui.parser.ParsedGui;
import me.jetby.libb.gui.parser.ParserRule;
import me.jetby.libb.test.PGuiTest;
import me.jetby.libb.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class LibbCommand extends AdvancedCommand {
    private final Libb plugin;
    public LibbCommand(Libb plugin) {
        super("libb", plugin);
        this.plugin = plugin;
    }
    @SubCommand("reload")
    @Permission("libb.command.reload")
    public void reload(CommandSender sender) {
        try {
            long start = System.currentTimeMillis();

            plugin.guisConfiguration.unregisterGuiCommands();
            plugin.guisConfiguration.load();
            Logger.info(plugin, "<#0AD7FB>▶ " + LibbApi.Settings.PARSED_GUIS.size() + " guis loaded");
            for (Player p : Bukkit.getOnlinePlayers()) {
                Inventory topInventory = p.getOpenInventory().getTopInventory();
                if (!(topInventory instanceof AdvancedGui)) continue;
                p.closeInventory();
            }
            sender.sendMessage(LibbApi.Settings.CONFIG_COLORIZER.deserialize("<green>Libb reloaded in " + (System.currentTimeMillis() - start) + " ms."));
        } catch (Exception e) {
            sender.sendMessage(LibbApi.Settings.CONFIG_COLORIZER.deserialize("<red>Reload error, check the console."));
            e.printStackTrace();
        }

    }

    @SubCommand("test")
    public void test(Player player) {
        new PGuiTest(player, plugin, LibbApi.Settings.PARSED_GUIS.get("test")).open(player);
    }

    @SubCommand("open")
    @InsufficientArgs("<red>Usage: /libb open <menu> <player>")
    @Permission("libb.command.open")
    public void open(Player sender, String[] args) {
        var guiDef = LibbApi.Settings.PARSED_GUIS.get(args[0]);
        if (guiDef == null) {
            sender.sendMessage(LibbApi.Settings.CONFIG_COLORIZER.deserialize("<red>Gui not found"));
            return;
        }
        Player target = sender;
        if (args.length > 1) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(LibbApi.Settings.CONFIG_COLORIZER.deserialize("<red>Target not found"));
                return;
            }
        }

        new ParsedGui(target, guiDef, plugin, ParserRule.of(LibbApi.Settings.CONFIG_COLORIZER)).open(target);
    }
    @TabComplete("open")
    public List<String> tabOpen(CommandSender sender, String[] args) {
        if (!sender.hasPermission("libb.command.open")) return List.of();
        return LibbApi.Settings.PARSED_GUIS.keySet().stream().toList();
    }
}
