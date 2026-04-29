package me.jetby.libb.command;

import me.jetby.libb.LibbApi;
import me.jetby.libb.command.annotations.Cooldown;
import me.jetby.libb.command.annotations.Permission;
import me.jetby.libb.command.annotations.PlayerOnly;
import me.jetby.libb.command.annotations.messages.Arg;
import me.jetby.libb.command.annotations.messages.InsufficientArgs;
import me.jetby.libb.util.CooldownUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

public class MethodSubCommand {
    private final Method method;
    private final MethodHandle handle;
    private final MethodHandle tabHandle;

    public MethodSubCommand(Method method, Object instance, Method tabMethod) {
        this.method = method;

        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
                    instance.getClass(), MethodHandles.lookup()
            );
            this.handle = lookup.unreflect(method).bindTo(instance);
            this.tabHandle = tabMethod != null ? lookup.unreflect(tabMethod).bindTo(instance) : null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to create MethodHandle for " + method.getName(), e);
        }
    }

    public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (!runGuards(sender, method)) return false;
        Object[] parsed = parseArgs(method, sender, args);
        if (parsed == null) return false;
        try {
            handle.invokeWithArguments(parsed);
            return true;
        } catch (Throwable e) {
            sender.sendMessage("Internal command error.");
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> tab(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (tabHandle == null) return List.of();
        try {
            return (List<String>) tabHandle.invokeWithArguments(sender, args);
        } catch (Throwable e) {
            return List.of();
        }
    }

    private boolean runGuards(CommandSender sender, Method method) {
        if (method.isAnnotationPresent(PlayerOnly.class)) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(method.getAnnotation(PlayerOnly.class).message());
                return false;
            }
        }
        if (method.isAnnotationPresent(Permission.class)) {
            Permission ann = method.getAnnotation(Permission.class);
            if (!sender.hasPermission(ann.value())) {
                sender.sendMessage(ann.message());
                return false;
            }
        }
        if (method.isAnnotationPresent(Cooldown.class) && sender instanceof Player player) {
            Cooldown ann = method.getAnnotation(Cooldown.class);
            long remaining = CooldownUtil.getRemaining(player.getUniqueId(), method.getName());
            if (remaining > 0) {
                sender.sendMessage(ann.message().replace("{remaining}", String.valueOf(remaining)));
                return false;
            }
            CooldownUtil.set(player.getUniqueId(), method.getName(), ann.seconds());
        }
        return true;
    }

    private Object[] parseArgs(Method method, CommandSender sender, String[] args) {
        Parameter[] params = method.getParameters();
        Object[] result = new Object[params.length];
        int argIndex = 0;

        String insufficientMsg = method.isAnnotationPresent(InsufficientArgs.class)
                ? method.getAnnotation(InsufficientArgs.class).value()
                : "Insufficient arguments.";

        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            Class<?> type = param.getType();

            if (type == CommandSender.class) {
                result[i] = sender;
                continue;
            }
            if (type == Player.class && argIndex == 0 && i == 0) {
                result[i] = sender;
                continue;
            }

            if (argIndex >= args.length) {
                sender.sendMessage(LibbApi.Settings.CONFIG_COLORIZER.deserialize(insufficientMsg));
                return null;
            }

            String raw = args[argIndex++];
            String errorMsg = getArgError(param, raw);

            if (type == String.class) {
                result[i] = raw;

            } else if (type == String[].class) {
                result[i] = Arrays.copyOfRange(args, argIndex - 1, args.length);
                argIndex = args.length;

            } else {
                String message = errorMsg.isEmpty() ? "'" + raw + "' is not a number." : errorMsg;

                if (type == int.class || type == Integer.class) {
                    try {
                        result[i] = Integer.parseInt(raw);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(LibbApi.Settings.CONFIG_COLORIZER.deserialize(message));
                        return null;
                    }

                } else if (type == double.class || type == Double.class) {
                    try {
                        result[i] = Double.parseDouble(raw);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(LibbApi.Settings.CONFIG_COLORIZER.deserialize(message));
                        return null;
                    }

                } else if (type == Player.class) {
                    Player target = Bukkit.getPlayer(raw);
                    if (target == null) {
                        if (errorMsg.isEmpty()) {
                            sender.sendMessage("Player '" + raw + "' not found.");
                        } else {
                            sender.sendMessage(LibbApi.Settings.CONFIG_COLORIZER.deserialize(message));
                        }
                        return null;
                    }
                    result[i] = target;

                } else if (type == boolean.class || type == Boolean.class) {
                    result[i] = raw.equalsIgnoreCase("true") || raw.equalsIgnoreCase("yes");

                } else {
                    sender.sendMessage("Unknown argument type: " + type.getSimpleName());
                    return null;
                }
            }
        }
        return result;
    }

    private String getArgError(Parameter param, String raw) {
        if (!param.isAnnotationPresent(Arg.class)) return "";
        return param.getAnnotation(Arg.class).error().replace("{input}", raw);
    }
}