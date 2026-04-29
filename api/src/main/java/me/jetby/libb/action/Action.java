package me.jetby.libb.action;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for action implementations — both built-in and custom ones from other plugins.
 *
 * <p>One class can handle multiple keys — just register it multiple times:</p>
 * <pre>{@code
 * MyHandler handler = new MyHandler();
 * ActionRegistry.register("myplugin", "my_a", handler);
 * ActionRegistry.register("myplugin", "my_b", handler);
 * }</pre>
 *
 * <p>Or use a lambda for simple actions:</p>
 * <pre>{@code
 * ActionRegistry.register("myplugin", "my_action", (ctx, text) -> {
 *     Entity e = ctx.get(Entity.class);
 *     ctx.getPlayer().sendMessage("Entity: " + e.getName());
 * });
 * }</pre>
 */
@FunctionalInterface
public interface Action {
    /**
     * Execute the action.
     *
     * @param ctx  context containing the player and any extra objects
     * @param input text after the key, already processed by PlaceholderAPI
     *             (e.g. for line {@code [message] Hello %player_name%} → {@code "Hello MrJetby"})
     *              <br>
     *              <br>
     *              Use {@code input.rawText()} to get String
     *              <br>
     *              Use {@code input.serialized()} to get Component
     */
    void execute(@NotNull ActionContext ctx, @NotNull ActionInput input);
}