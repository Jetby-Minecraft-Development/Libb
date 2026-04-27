package me.jetby.libb.action;

import me.clip.placeholderapi.PlaceholderAPI;
import me.jetby.libb.Libb;
import me.jetby.libb.action.events.PreActionExecuteEvent;
import me.jetby.libb.action.record.ActionBlock;
import me.jetby.libb.action.record.Expression;
import me.jetby.libb.color.Serializer;
import me.jetby.libb.color.SerializerType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Entry point for executing actions.
 *
 * <pre>{@code
 * // Simple run
 * ActionExecute.run(ActionContext.of(player), "[message] Hello!");
 *
 * // Explicit namespace
 * ActionExecute.run(ActionContext.of(player), "[myplugin:spawn] some text");
 *
 * // With extra objects in context
 * ActionExecute.run(
 *     ActionContext.of(player).with(entity),
 *     "[myplugin:spawn] some text"
 * );
 * }</pre>
 */
@SuppressWarnings("unused")
public final class ActionExecute {

    public static void run(@NotNull ActionContext ctx, @NotNull String line, @Nullable Serializer serializer) {
        String namespaceHint = ctx.getPlugin() != null
                ? ctx.getPlugin().getName().toLowerCase()
                : null;

        String key = ActionRegistry.resolveKey(line, namespaceHint);
        if (key == null) return;

        Action handler = ActionRegistry.resolve(line, namespaceHint);
        if (handler == null) return;

        // apply replacements
        String rawText = ActionRegistry.extractText(line, key);
        for (Map.Entry<CharSequence, CharSequence> c : ctx.getAllReplace().entrySet()) {
            rawText = rawText.replace(c.getKey(), c.getValue());
        }

        // apply placeholders
        String text = PlaceholderAPI.setPlaceholders(ctx.getPlayer(), rawText);

        Component component = null;
        if (serializer!=null) {
            component = serializer.deserialize(text);
        }
        ctx.setSerializer(serializer);

        // call PreActionExecuteEvent event
        Bukkit.getScheduler().runTask(Libb.INSTANCE,
                () -> Bukkit.getPluginManager().callEvent(new PreActionExecuteEvent(ctx, key)));

        handler.execute(ctx, new ActionInput(text, component));
    }
    public static void run(@NotNull ActionContext ctx, @NotNull String line) {
        run(ctx, line, null);
    }

    public static void run(@NotNull ActionContext ctx, @NotNull List<String> list) {
        scheduleChain(ctx, new ArrayList<>(list), 0, 0);
    }

    public static void run(@NotNull ActionContext ctx,
                           @NotNull ActionBlock block) {

        List<Object> items = new ArrayList<>(block.staticActions());
        items.addAll(block.expressions());

        scheduleChain(ctx, items, 0, 0);
    }

    public static void run(@NotNull ActionContext ctx, @NotNull Expression expression) {
        String input = expression.input();
        for (Map.Entry<CharSequence, CharSequence> c : ctx.getAllReplace().entrySet()) {
            input = input.replace(c.getKey(), c.getValue());
        }

        boolean result = evaluate(ctx.getPlayer(), input);
        Iterable<String> lines = result ? expression.success() : expression.fail();
        for (String line : lines) {
            run(ctx, line);
        }
    }

    /**
     * Recursively schedule batches separated by [delay] entries.
     *
     * @param items            remaining items to process
     * @param batchStart       index of the first item in the current batch
     * @param accumulatedDelay total ticks elapsed so far
     */
    private static void scheduleChain(@NotNull ActionContext ctx,
                                      @NotNull List<Object> items,
                                      int batchStart,
                                      long accumulatedDelay) {
        if (batchStart >= items.size()) return;

        List<Object> batch = new ArrayList<>();
        long nextDelay = -1;
        int nextBatchStart = items.size();

        for (int i = batchStart; i < items.size(); i++) {
            Object item = items.get(i);
            if (item instanceof String line) {
                long ticks = parseDelay(line);
                if (ticks >= 0) {
                    nextDelay = ticks;
                    nextBatchStart = i + 1;
                    break;
                }
            }
            batch.add(item);
        }

        Runnable executeBatch = () -> {
            for (Object item : batch) {
                if (item instanceof String line) {
                    run(ctx, line);
                } else if (item instanceof Expression expression) {
                    run(ctx, expression);
                }
            }
        };

        if (accumulatedDelay <= 0) {
            executeBatch.run();
        } else {
            Bukkit.getScheduler().runTaskLater(Libb.INSTANCE, executeBatch, accumulatedDelay);
        }

        if (nextDelay >= 0) {
            final int finalNextBatchStart = nextBatchStart;
            final long finalNextDelay = accumulatedDelay + nextDelay;
            if (accumulatedDelay <= 0) {
                scheduleChain(ctx, items, finalNextBatchStart, finalNextDelay);
            } else {
                Bukkit.getScheduler().runTaskLater(Libb.INSTANCE, () ->
                                scheduleChain(ctx, items, finalNextBatchStart, finalNextDelay),
                        accumulatedDelay
                );
            }
        }
    }

    private static long parseDelay(@NotNull String line) {
        String key = ActionRegistry.resolveKey(line, ActionRegistry.LIBB);
        if (key == null) return -1;

        if (!key.equals("delay") && !key.equals("libb:delay")) return -1;

        String text = ActionRegistry.extractText(line, key).trim();
        try {
            long ticks = Long.parseLong(text);
            return Math.max(ticks, 0);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static boolean evaluate(Player player, @NotNull String input) {
        String[] parts = input.split(" ", 3);
        if (parts.length < 3) return false;

        String left = player != null ? PlaceholderAPI.setPlaceholders(player, parts[0]) : parts[0];
        String op = parts[1];
        String right = player != null ? PlaceholderAPI.setPlaceholders(player, parts[2]) : parts[2];

        try {
            double l = Double.parseDouble(left);
            double r = Double.parseDouble(right);
            return switch (op) {
                case ">" -> l > r;
                case ">=" -> l >= r;
                case "==" -> Math.abs(l - r) < 1e-9;
                case "!=" -> l != r;
                case "<=" -> l <= r;
                case "<" -> l < r;
                default -> false;
            };
        } catch (NumberFormatException ignored) {
        }

        return switch (op) {
            case "==" -> left.equals(right);
            case "!=" -> !left.equals(right);
            default -> false;
        };
    }
}