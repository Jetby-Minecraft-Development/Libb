package me.jetby.libb.executors;

import me.jetby.libb.Libb;
import me.jetby.libb.gui.AdvancedGui;
import me.jetby.libb.gui.parser.ParsedGui;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class LibbCommand implements CommandExecutor {
    private final Libb plugin;

    public LibbCommand(Libb plugin) {
        this.plugin = plugin;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (sender instanceof Player player) {

            if (args[0].equalsIgnoreCase("reload")) {
                plugin.menusLoader.load();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Inventory topInventory = p.getOpenInventory().getTopInventory();
                    if (!(topInventory instanceof AdvancedGui)) continue;
                    p.closeInventory();
                }
                player.sendMessage("successfully reloaded");
                return true;
            }
            if (args[0].equalsIgnoreCase("test")) {
                ParsedGui gui = new ParsedGui(player, Libb.PARSED_GUIS.get(args[1]))
                        .addClickHandler("test", event -> {
                            event.setCancelled(true);
                            event.getWhoClicked().sendMessage(event.getSection().getString("test"));
                        });
                gui.open(player);
                return true;
            }

        }

        return true;
    }
}
