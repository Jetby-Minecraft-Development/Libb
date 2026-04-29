package me.jetby.libb.action;

import lombok.Getter;
import lombok.Setter;
import me.jetby.libb.LibbApi;
import me.jetby.libb.color.Serializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Type-safe container for objects passed into an action.
 *
 * <pre>{@code
 * // Creating
 * ActionContext ctx = ActionContext.of(player)
 *         .with(someEntity)
 *         .with(myGui);
 *
 * // Reading inside a handler
 * Entity e = ctx.get(Entity.class); // null if not provided
 * }</pre>
 */
@Getter
public class ActionContext {

    private final Player player;
    @Nullable
    private JavaPlugin plugin;

    @Nullable
    @Setter
    private Serializer serializer;


    @NotNull
    public Serializer getSerializer() {
        return serializer == null ? LibbApi.Settings.CONFIG_COLORIZER : serializer;
    }

    private final Map<Class<?>, Object> objects = new HashMap<>();

    private final Map<CharSequence, CharSequence> toReplace = new LinkedHashMap<>();

    public Map<CharSequence, CharSequence> getAllReplace() {
        return toReplace;
    }

    private ActionContext() {
        this.player = null;
    }

    private ActionContext(@Nullable Player player) {
        this.player = player;
    }

    private ActionContext(@Nullable Player player, @Nullable JavaPlugin plugin, @Nullable Serializer serializer) {
        this.player = player;
        this.plugin = plugin;
        this.serializer = serializer;
    }

    public static ActionContext of(@Nullable Player player) {
        return new ActionContext(player);
    }

    public static ActionContext of(@Nullable Player player, @Nullable Serializer serializer) {
        return new ActionContext(player, null, serializer);
    }

    public static ActionContext of(@Nullable Player player, @Nullable JavaPlugin plugin) {
        if (plugin == null) {
            return new ActionContext(player);
        }
        return new ActionContext(player, plugin, null);
    }
    public static ActionContext of(@Nullable Player player, @Nullable JavaPlugin plugin, @Nullable Serializer serializer) {
        if (plugin == null) {
            return new ActionContext(player);
        }
        return new ActionContext(player, plugin, serializer);
    }

    /**
     * Registers a placeholder replacement, similar to {@link String#replace(CharSequence, CharSequence)}.
     *
     * @param key   the placeholder to replace
     * @param value the value to substitute
     * @return this instance for chaining
     */
    public ActionContext replace(CharSequence key, CharSequence value) {
        toReplace.put(key, value);
        return this;
    }

    public ActionContext replaceFromMap(Map<String, String> map) {
        toReplace.putAll(map);
        return this;
    }

    /**
     * Add an object to the context. The key is the object's class.
     * If two objects of the same class are added, the second overwrites the first.
     */
    public <T> ActionContext with(@NotNull T object) {
        objects.put(object.getClass(), object);
        return this;
    }

    /**
     * Add an object under an explicit class key.
     * Useful when you want to retrieve it by interface rather than concrete class.
     *
     * <pre>{@code ctx.with(MyEntity.class, entity); }</pre>
     */
    public <T> ActionContext with(@NotNull Class<T> key, @NotNull T object) {
        objects.put(key, object);
        return this;
    }

    /**
     * Get an object by class. Returns {@code null} if it was not added.
     */
    @Nullable
    public <T> T get(@NotNull Class<T> type) {
        return type.cast(objects.get(type));
    }

    /**
     * Get an object by class or throw if it is missing.
     * Use when the object is required for the handler to work.
     */
    @NotNull
    public <T> T require(@NotNull Class<T> type) {
        T value = get(type);
        if (value == null) {
            throw new IllegalStateException(
                    "ActionContext is missing required object of type: " + type.getSimpleName());
        }
        return value;
    }

    public boolean has(@NotNull Class<?> type) {
        return objects.containsKey(type);
    }
}