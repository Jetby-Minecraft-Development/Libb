package me.jetby.libb.command;

import me.jetby.libb.Libb;
import me.jetby.libb.command.annotations.Permission;
import me.jetby.libb.command.annotations.SubCommand;
import me.jetby.libb.command.annotations.TabComplete;
import me.jetby.libb.command.annotations.messages.InsufficientArgs;
import me.jetby.libb.gui.AdvancedGui;
import me.jetby.libb.gui.parser.ParsedGui;
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
            for (Player p : Bukkit.getOnlinePlayers()) {
                Inventory topInventory = p.getOpenInventory().getTopInventory();
                if (!(topInventory instanceof AdvancedGui)) continue;
                p.closeInventory();
            }
            sender.sendMessage(Libb.MINI_MESSAGE.deserialize("<green>Libb reloaded in " + (System.currentTimeMillis() - start) + " ms."));
        } catch (Exception e) {
            sender.sendMessage(Libb.MINI_MESSAGE.deserialize("<red>Reload error, check the console."));
            e.printStackTrace();
        }

    }

    @SubCommand("open")
    @InsufficientArgs("<red>Usage: /libb open <menu> <player>")
    @Permission("libb.command.open")
    public void open(Player sender, String[] args) {
        var guiDef = Libb.PARSED_GUIS.get(args[0]);
        if (guiDef == null) {
            sender.sendMessage(Libb.MINI_MESSAGE.deserialize("<red>Gui not found"));
            return;
        }
        Player target = sender;
        if (args.length > 1) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(Libb.MINI_MESSAGE.deserialize("<red>Target not found"));
                return;
            }
        }

        new ParsedGui(target, guiDef, plugin).open(target);
    }

    @TabComplete({"open"})
    public List<String> tabOpen(CommandSender sender, String[] args) {
        if (!sender.hasPermission("libb.command.open")) return List.of();
        return Libb.PARSED_GUIS.keySet().stream().toList();
    }
}
