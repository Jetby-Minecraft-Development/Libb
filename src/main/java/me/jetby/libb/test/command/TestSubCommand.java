package me.jetby.libb.test.command;

import me.jetby.libb.command.annotations.Permission;
import me.jetby.libb.command.annotations.PlayerOnly;
import me.jetby.libb.command.annotations.SubCommand;
import me.jetby.libb.command.annotations.TabComplete;
import me.jetby.libb.command.annotations.messages.InsufficientArgs;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TestSubCommand {

    @SubCommand("test2")
    public void test(CommandSender sender) {
        sender.sendMessage("Argument test2 also works");
    }

    @SubCommand({"set", "type"})
    @Permission(value = "cmd.set", message = "You dont have permission")
    @InsufficientArgs("<red>Usage: /test_command <white>set <type> <location>")
    @PlayerOnly(message = "Players only command")
    public void setType(Player sender, String type) {
        sender.sendMessage(type);
    }

    @SubCommand({"set", "location"})
    @Permission("cmd.set")
    @PlayerOnly
    public void setLocation(Player sender, String locationName) {
        sender.sendMessage(locationName);
    }


    @TabComplete({"set", "type"})
    public List<String> tabSetType(CommandSender sender, String[] args) {
        return List.of("VILLAGE", "DUNGEON", "CASTLE");
    }

    @TabComplete({"set", "location"})
    public List<String> tabSetLocation(CommandSender sender, String[] args) {
        return List.of("256_100_99", "9_0_11", "2301_150_2390");
    }
}
