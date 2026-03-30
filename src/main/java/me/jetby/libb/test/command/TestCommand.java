package me.jetby.libb.test.command;

import me.jetby.libb.command.AdvancedCommand;
import me.jetby.libb.command.annotations.PlayerOnly;
import me.jetby.libb.command.annotations.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class TestCommand extends AdvancedCommand {

    public TestCommand(JavaPlugin plugin) {
        super("test_command", plugin);
    }

    @Override
    public boolean onExecute(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("Success");
        return true;
    }

    @Override
    public List<String> onTab(CommandSender sender, Command command, String label, String[] args) {
        return List.of("help");
    }

    @SubCommand("test")
    @PlayerOnly
    public void test(Player player) {
        player.sendMessage("Test argument successfully works");
    }
}
