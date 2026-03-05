package me.jetby.libb.executors;

import me.jetby.libb.Libb;
import me.jetby.libb.gui.parser.ParsedGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                player.sendMessage("successfully reloaded");
                return true;
            }
            if (args[0].equalsIgnoreCase("test")) {
                ParsedGui gui = new ParsedGui(player, Libb.PARSED_GUIS.get(args[1]))
                        .setItemHandler("test", (event, section) -> {
                            event.setCancelled(true);
                            event.getWhoClicked().sendMessage(section.getString("test"));
                        });
                gui.getGui().open(player);
                return true;
            }

        }

        return true;
    }
}
