package me.jetby.libb.command;

import org.bukkit.Bukkit;
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
import java.util.logging.Level;

public class CommandRegistrar extends BukkitCommand {


    private static final Map<String, BukkitCommand> registeredCommands = new HashMap<>();

    protected CommandRegistrar(@NotNull String name) {
        super(name);
    }

    protected CommandRegistrar(@NotNull String name, @NotNull String description, @NotNull String usageMessage, @NotNull List<String> aliases) {
        super(name, description, usageMessage, aliases);
    }

    public static void registerCommand(JavaPlugin plugin, String commandName, @NotNull CommandExecutor executor) {

        PluginCommand command = plugin.getServer().getPluginCommand(commandName);
        if (command == null) {
            plugin.getLogger().log(Level.WARNING, "You are attempting to register the command " + commandName + ", but you have not added it to plugin.yml.");
            return;
        }

        try {
            Field commandMapField = plugin.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(plugin.getServer());
            BukkitCommand cmd = getBukkitCommand(commandName, executor);
            unregisterCommand(plugin, commandName, commandMap);
            commandMap.register(plugin.getName(), cmd);
            registeredCommands.put(commandName.toLowerCase(), cmd);

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    Method syncCommands = plugin.getServer().getClass().getDeclaredMethod("syncCommands");
                    syncCommands.setAccessible(true);
                    syncCommands.invoke(plugin.getServer());
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to sync commands", e);
                }
            });

        } catch (NoSuchFieldException | IllegalAccessException e) {
            plugin.getLogger().log(Level.WARNING, "Error with command registration", e);
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


    @SuppressWarnings("unchecked")
    public static void unregisterCommand(JavaPlugin plugin, String commandName, CommandMap commandMap) {
        try {
            Field knownCommandsField;
            try {
                knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            } catch (NoSuchFieldException e) {
                knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
            }

            knownCommandsField.setAccessible(true);
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            Command existing = knownCommands.remove(commandName.toLowerCase());
            if (existing != null) {
                existing.unregister(commandMap);
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error with command unregistration", e);
        }
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
                    plugin.getLogger().log(Level.WARNING, "Failed to sync commands", e);
                }
            });

        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error with commands unregistration", e);
        }
    }

    public static void unregisterAll() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            for (BukkitCommand cmd : registeredCommands.values()) {
                cmd.unregister(commandMap);
            }
            registeredCommands.clear();

        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "[Libb] Error with commands unregistration", e);
        }
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        return false;
    }
}
