package me.jetby.libb.command;

import lombok.Getter;
import me.jetby.libb.command.annotations.SubCommand;
import me.jetby.libb.command.annotations.TabComplete;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;

@Getter
public abstract class AdvancedCommand implements CommandExecutor, TabCompleter {
    private final String commandName;
    private final JavaPlugin plugin;
    private final CommandNode root = new CommandNode();

    public AdvancedCommand(String commandName, JavaPlugin plugin) {
        this.plugin = plugin;
        this.commandName = commandName;
        scanMethods(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        CommandNode node = root;
        int depth = 0;

        for (int i = 0; i < args.length; i++) {
            CommandNode child = node.get(args[i]);
            if (child == null) break;
            node = child;
            depth = i + 1;
        }

        if (node.getExecutor() != null) {
            String[] remaining = Arrays.copyOfRange(args, depth, args.length);
            return node.getExecutor().execute(sender, command, label, remaining);
        }

        return onExecute(sender, command, label, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        CommandNode node = root;
        int depth = 0;
        for (int i = 0; i < args.length - 1; i++) {
            CommandNode child = node.get(args[i]);
            if (child == null) break;
            node = child;
            depth = i + 1;
        }

        String current = args[args.length - 1].toLowerCase();
        List<String> suggestions = new ArrayList<>();

        for (String key : node.childrenKeys()) {
            if (key.startsWith(current)) suggestions.add(key);
        }

        if (node.getExecutor() != null) {
            String[] remaining = Arrays.copyOfRange(args, depth, args.length);
            List<String> custom = node.getExecutor().tab(sender, command, label, remaining);
            if (custom != null) suggestions.addAll(
                    custom.stream().filter(s -> s.toLowerCase().startsWith(current)).toList()
            );
        }

        return suggestions;
    }

    protected boolean onExecute(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        return true;
    }

    protected List<String> onTab(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        return List.of();
    }

    public AdvancedCommand register() {
        CommandRegistrar.registerCommand(plugin, commandName, this);
        return this;
    }

    public AdvancedCommand addSubCommand(Object... instances) {
        for (Object instance : instances) scanMethods(instance);
        return this;
    }

    public AdvancedCommand addSubCommand(Object instance) {
        scanMethods(instance);
        return this;
    }

    private void scanMethods(Object instance) {
        Map<String, Method> tabMethods = new HashMap<>();
        for (Method m : instance.getClass().getDeclaredMethods()) {
            if (!m.isAnnotationPresent(TabComplete.class)) continue;
            m.setAccessible(true);
            String key = String.join(".", m.getAnnotation(TabComplete.class).value()).toLowerCase();
            tabMethods.put(key, m);
        }

        for (Method m : instance.getClass().getDeclaredMethods()) {
            if (!m.isAnnotationPresent(SubCommand.class)) continue;
            m.setAccessible(true);
            String[] path = m.getAnnotation(SubCommand.class).value();
            String tabKey = String.join(".", path).toLowerCase();
            Method tabMethod = tabMethods.get(tabKey);

            CommandNode node = root;
            for (String segment : path) {
                node = node.getOrCreate(segment);
            }
            node.setExecutor(new MethodSubCommand(m, instance, tabMethod));
        }
    }
}