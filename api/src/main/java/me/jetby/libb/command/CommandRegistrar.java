package me.jetby.libb.command;

import me.jetby.libb.util.Logger;
import org.bukkit.command.*;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandRegistrar extends BukkitCommand {


    private static final Map<String, BukkitCommand> registeredCommands = new HashMap<>();

    protected CommandRegistrar(@NotNull String name) {
        super(name);
    }

    protected CommandRegistrar(@NotNull String name, @NotNull String description, @NotNull String usageMessage, @NotNull List<String> aliases) {
        super(name, description, usageMessage, aliases);
    }

    public static void registerCommand(JavaPlugin plugin, String commandName, @NotNull CommandExecutor executor) {

        try {
            Field commandMapField = plugin.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(plugin.getServer());
            BukkitCommand cmd = getBukkitCommand(commandName, executor);
            unregisterCommand(plugin, commandName);
            commandMap.register(plugin.getName(), cmd);
            registeredCommands.put(commandName.toLowerCase(), cmd);

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    Method syncCommands = plugin.getServer().getClass().getDeclaredMethod("syncCommands");
                    syncCommands.setAccessible(true);
                    syncCommands.invoke(plugin.getServer());
                } catch (Exception e) {
                    Logger.warn(plugin, "Failed to sync commands", e);
                }
            });

        } catch (NoSuchFieldException | IllegalAccessException e) {
            Logger.warn(plugin, "Error with command registration", e);
        }
    }

    private static @NotNull BukkitCommand getBukkitCommand(String commandName, @NotNull CommandExecutor executor) {
        BukkitCommand cmd = new BukkitCommand(commandName) {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
                return executor.onCommand(sender, this, label, args);
            }

            @Override
            public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
                if (executor instanceof TabCompleter tabCompleter) {
                    List<String> result = tabCompleter.onTabComplete(sender, this, alias, args);
                    return result != null ? result : super.tabComplete(sender, alias, args);
                }
                return super.tabComplete(sender, alias, args);
            }
        };

        cmd.setAliases(Collections.emptyList());
        return cmd;
    }


    public static void unregisterCommand(JavaPlugin plugin, String commandName) {
        try {
            Field commandMapField = plugin.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(plugin.getServer());

            Command existing = getCommand(plugin, commandName, commandMap);
            if (existing != null) {
                existing.unregister(commandMap);
            }

        } catch (Exception e) {
            Logger.warn(plugin, "Error with command unregistration", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Command getCommand(JavaPlugin plugin, String commandName, CommandMap commandMap) throws NoSuchFieldException, IllegalAccessException {
        Field knownCommandsField;
        try {
            knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
        } catch (NoSuchFieldException e) {
            knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
        }

        knownCommandsField.setAccessible(true);
        Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                Method syncCommands = plugin.getServer().getClass().getDeclaredMethod("syncCommands");
                syncCommands.setAccessible(true);
                syncCommands.invoke(plugin.getServer());
            } catch (Exception e) {
                Logger.warn(plugin, "Failed to sync commands", e);
            }
        });

        return knownCommands.remove(commandName.toLowerCase());
    }


    public static void unregisterAll(JavaPlugin plugin) {
        try {
            Field commandMapField = plugin.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(plugin.getServer());

            for (BukkitCommand cmd : registeredCommands.values()) {
                cmd.unregister(commandMap);
            }

            Field knownCommandsField;
            try {
                knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            } catch (NoSuchFieldException e) {
                knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
            }
            knownCommandsField.setAccessible(true);
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
            registeredCommands.keySet().forEach(name -> {
                knownCommands.remove(name);
                knownCommands.remove(plugin.getName().toLowerCase() + ":" + name);
            });

            registeredCommands.clear();

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    Method syncCommands = plugin.getServer().getClass().getDeclaredMethod("syncCommands");
                    syncCommands.setAccessible(true);
                    syncCommands.invoke(plugin.getServer());
                } catch (Exception e) {
                    Logger.warn(plugin, "Failed to sync commands", e);
                }
            });

        } catch (Exception e) {
            Logger.error(plugin, "Error with commands unregistration", e);
        }
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        return false;
    }
}
