package me.jetby.libb.action;

import me.clip.placeholderapi.PlaceholderAPI;
import me.jetby.libb.Libb;
import me.jetby.libb.action.record.ActionBlock;
import me.jetby.libb.action.record.Expression;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
public final class ActionExecute {

    private ActionExecute() {
    }

    public static void run(@NotNull ActionContext ctx, @NotNull String line) {
        String key = ActionRegistry.resolveKey(line);
        if (key == null) return;

        Action handler = ActionRegistry.resolve(line);
        if (handler == null) return;

        String rawText = ActionRegistry.extractText(line, key);
        String text = ctx.getPlayer() != null
                ? PlaceholderAPI.setPlaceholders(ctx.getPlayer(), rawText)
                : rawText;
        handler.execute(ctx, text);
    }

    public static void run(@NotNull ActionContext ctx,
                           @NotNull ActionBlock block) {

        List<Object> items = new ArrayList<>();
        items.addAll(block.staticActions());
        for (Expression expression : block.expressions()) {
            items.add(expression);
        }

        scheduleChain(ctx, items, 0, 0);
    }
    public static void run(@NotNull ActionContext ctx, @NotNull Expression expression) {
        boolean result = evaluate(ctx.getPlayer(), expression.expression());
        Iterable<String> lines = result ? expression.success() : expression.fail();
        for (String line : lines) {
            run(ctx, line);
        }
    }

    /**
     * Recursively schedule batches separated by [delay] entries.
     *
     * @param items        remaining items to process
     * @param batchStart   index of the first item in the current batch
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
            Bukkit.getScheduler().runTaskLater(Libb.getInstance(), executeBatch, accumulatedDelay);
        }

        if (nextDelay >= 0) {
            final int finalNextBatchStart = nextBatchStart;
            final long finalNextDelay = accumulatedDelay + nextDelay;
            if (accumulatedDelay <= 0) {
                scheduleChain( ctx, items, finalNextBatchStart, finalNextDelay);
            } else {
                Bukkit.getScheduler().runTaskLater(Libb.getInstance(), () ->
                                scheduleChain(ctx, items, finalNextBatchStart, finalNextDelay),
                        accumulatedDelay
                );
            }
        }
    }

    private static long parseDelay(@NotNull String line) {
        String key = ActionRegistry.resolveKey(line);
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
                case "==" -> l == r;
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