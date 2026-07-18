package org.jetby.libb.action;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetby.libb.action.events.PreActionExecuteEvent;
import org.jetby.libb.action.record.ActionBlock;
import org.jetby.libb.action.record.Expression;
import org.jetby.libb.color.Serializer;

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
        if (serializer != null) {
            component = serializer.deserialize(text);
        }
        ctx.setSerializer(serializer);

        // call {@link PreActionExecuteEvent} event
        Bukkit.getPluginManager().callEvent(new PreActionExecuteEvent(ctx, key));

        handler.execute(ctx, new ActionInput(text, component));
    }

    public static void run(@NotNull String line) {
        run(new ActionContext(), line, null);
    }

    public static void run(@NotNull List<String> list) {
        ActionUtil.scheduleChain(new ActionContext(), new ArrayList<>(list), 0, 0);
    }
    public static void run(@NotNull ActionContext ctx, @NotNull String line) {
        run(ctx, line, null);
    }

    public static void run(@NotNull ActionContext ctx, @NotNull List<String> list) {
        ActionUtil.scheduleChain(ctx, new ArrayList<>(list), 0, 0);
    }

    public static void run(@NotNull ActionContext ctx,
                           @NotNull ActionBlock block) {

        List<Object> items = new ArrayList<>(block.staticActions());
        items.addAll(block.expressions());

        ActionUtil.scheduleChain(ctx, items, 0, 0);
    }

    public static boolean run(@NotNull ActionContext ctx, @NotNull Expression expression) {
        String input = expression.input();
        for (Map.Entry<CharSequence, CharSequence> c : ctx.getAllReplace().entrySet()) {
            input = input.replace(c.getKey(), c.getValue());
        }

        boolean result = ActionUtil.evaluate(ctx.getPlayer(), input);
        Iterable<String> lines = result ? expression.success() : expression.fail();
        for (String line : lines) {
            run(ctx, line);
        }
        return result;
    }

    public static boolean run(@NotNull ActionContext ctx, @NotNull List<Expression> expressions, ActionUtil.EvaluateMode mode) {
        int matched = 0;
        for (Expression expression : expressions) {
            if (run(ctx, expression)) matched++;
        }
        return switch (mode) {
            case ALL -> matched == expressions.size();
            case AT_LEAST_ONE -> matched >= 1;
            case ONE_ONLY -> matched == 1;
        };
    }

}