package me.jetby.libb.action;

import me.jetby.libb.action.impl.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry for all available actions with namespace support.
 *
 * <h3>Key format in config</h3>
 * <pre>
 *   [spawn]              → first registered handler with command "spawn" (any namespace)
 *   [myplugin:spawn]     → explicit namespace, resolves only myplugin's handler
 *   [otherplugin:spawn]  → explicit namespace, resolves only otherplugin's handler — no conflicts!
 * </pre>
 *
 * <h3>Registering from another plugin</h3>
 * <pre>{@code
 * // onEnable
 * ActionRegistry.register("myplugin", "spawn", (ctx, text) -> {
 *     ctx.getPlayer().teleport(spawnLocation);
 * });
 *
 * // onDisable
 * ActionRegistry.unregisterAll("myplugin");
 * }</pre>
 */
public final class ActionRegistry {

    private ActionRegistry() {
    }

    private static final Map<String, Action> HANDLERS = new LinkedHashMap<>();

    public static final String LIBB = "libb";

    static {
        register(LIBB, "message", new MessageImpl());
        register(LIBB, "action_bar", new ActionBarImpl());
        register(LIBB, "actionbar", new ActionBarImpl());
        register(LIBB, "broadcast_action_bar", new BroadcastActionBarImpl());
        register(LIBB, "broadcastactionbar", new BroadcastActionBarImpl());
        register(LIBB, "broadcast_message", new BroadcastMessageImpl());
        register(LIBB, "broadcastmessage", new BroadcastMessageImpl());
        register(LIBB, "broadcast_sound", new BroadcastSoundImpl());
        register(LIBB, "broadcastsound", new BroadcastSoundImpl());
        register(LIBB, "broadcast_title", new BroadcastTitleImpl());
        register(LIBB, "broadcasttitle", new BroadcastTitleImpl());
        register(LIBB, "console", new ConsoleImpl());
        register(LIBB, "effect", new EffectImpl());
        register(LIBB, "player", new PlayerImpl());
        register(LIBB, "open", new OpenImpl());
        register(LIBB, "title", new TitleImpl());
        register(LIBB, "sound", new SoundImpl());
        register(LIBB, "refresh", new RefreshImpl());
        register(LIBB, "delay", new DelayImpl());
    }

    /**
     * Register a handler under the given namespace and command.
     * The full internal key is {@code "namespace:command"}.
     *
     * @throws IllegalArgumentException if this namespace:command is already registered
     */
    public static void register(@NotNull String namespace, @NotNull String command, @NotNull Action handler) {
        String full = fullKey(namespace, command);
        if (HANDLERS.containsKey(full)) {
            throw new IllegalArgumentException("Action '" + full + "' is already registered!");
        }
        HANDLERS.put(full, handler);
    }

    /**
     * Overwrite a handler without throwing — use for overriding built-ins or hot-swapping.
     */
    public static void override(@NotNull String namespace, @NotNull String command, @NotNull Action handler) {
        HANDLERS.put(fullKey(namespace, command), handler);
    }

    /**
     * Get a handler directly by namespace and command.
     */
    @Nullable
    public static Action get(@NotNull String namespace, @NotNull String command) {
        return HANDLERS.get(fullKey(namespace, command));
    }

    /**
     * Unregister a specific handler. Call this in {@code onDisable}.
     */
    public static boolean unregister(@NotNull String namespace, @NotNull String command) {
        return HANDLERS.remove(fullKey(namespace, command)) != null;
    }

    /**
     * Unregister all handlers belonging to a plugin at once.
     * Convenient when you registered many commands.
     *
     * <pre>{@code
     * // onDisable
     * ActionRegistry.unregisterAll("myplugin");
     * }</pre>
     */
    public static void unregisterAll(@NotNull String namespace) {
        String prefix = namespace.toLowerCase() + ":";
        HANDLERS.keySet().removeIf(key -> key.startsWith(prefix));
    }

    /**
     * Find a handler by action line.
     *
     * <ul>
     *   <li>{@code [myplugin:spawn] text} — explicit namespace, exact match</li>
     *   <li>{@code [spawn] text}           — first registered handler with that command</li>
     * </ul>
     */
    @Nullable
    public static Action resolve(@NotNull String line, @Nullable String namespaceHint) {
        String lower = line.toLowerCase();

        for (Map.Entry<String, Action> entry : HANDLERS.entrySet()) {
            if (lower.startsWith("[" + entry.getKey() + "]")) {
                return entry.getValue();
            }
        }

        if (namespaceHint != null) {
            String hint = namespaceHint.toLowerCase();
            for (Map.Entry<String, Action> entry : HANDLERS.entrySet()) {
                String ns = namespacePart(entry.getKey());
                String cmd = commandPart(entry.getKey());
                if (ns.equals(hint) && lower.startsWith("[" + cmd + "]")) {
                    return entry.getValue();
                }
            }
        }

        for (Map.Entry<String, Action> entry : HANDLERS.entrySet()) {
            if (namespacePart(entry.getKey()).equals(LIBB)
                    && lower.startsWith("[" + commandPart(entry.getKey()) + "]")) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Extract the matched key from an action line — needed for {@link #extractText}.
     * Returns the full key {@code "ns:cmd"} or the short form {@code "cmd"}.
     */
    @Nullable
    public static String resolveKey(@NotNull String line, @Nullable String namespaceHint) {
        String lower = line.toLowerCase();

        for (String full : HANDLERS.keySet()) {
            if (lower.startsWith("[" + full + "]")) return full;
        }

        if (namespaceHint != null) {
            String hint = namespaceHint.toLowerCase();
            for (String full : HANDLERS.keySet()) {
                String ns = namespacePart(full);
                String cmd = commandPart(full);
                if (ns.equals(hint) && lower.startsWith("[" + cmd + "]")) return cmd;
            }
        }

        for (String full : HANDLERS.keySet()) {
            if (namespacePart(full).equals(LIBB)) {
                String cmd = commandPart(full);
                if (lower.startsWith("[" + cmd + "]")) return cmd;
            }
        }

        return null;
    }

    private static String namespacePart(String fullKey) {
        int idx = fullKey.indexOf(':');
        return idx >= 0 ? fullKey.substring(0, idx) : "";
    }

    /**
     * Extract the text after the key from an action line.
     * {@code "[myplugin:spawn] some text"} → {@code "some text"}
     */
    @NotNull
    public static String extractText(@NotNull String line, @NotNull String matchedKey) {
        String text = line.substring(matchedKey.length() + 2); // +2 for '[' and ']'
        return text.startsWith(" ") ? text.substring(1) : text;
    }

    private static String fullKey(String namespace, String command) {
        return namespace.toLowerCase() + ":" + command.toLowerCase();
    }

    private static String commandPart(String fullKey) {
        int idx = fullKey.indexOf(':');
        return idx >= 0 ? fullKey.substring(idx + 1) : fullKey;
    }
}