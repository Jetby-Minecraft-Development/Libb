package me.jetby.libb.action;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
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
public class ActionContext {

    @Getter
    private final Player player;
    @Getter @Nullable
    private JavaPlugin plugin;

    private final Map<Class<?>, Object> objects = new HashMap<>();

    private ActionContext(Player player) {
        this.player = player;
    }

    private ActionContext(Player player, @Nullable JavaPlugin plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public static ActionContext of(@Nullable Player player) {
        return new ActionContext(player);
    }

    public static ActionContext of(@Nullable Player player, @Nullable JavaPlugin plugin) {
        if (plugin==null) {
            return new ActionContext(player);
        }
        return new ActionContext(player, plugin);
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