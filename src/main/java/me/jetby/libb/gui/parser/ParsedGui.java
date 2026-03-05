package me.jetby.libb.gui.parser;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.jetby.libb.Libb;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionExecute;
import me.jetby.libb.action.record.ActionBlock;
import me.jetby.libb.gui.AdvancedGui;
import me.jetby.libb.gui.item.ItemWrapper;
import me.jetby.libb.gui.parser.view.ItemPatchApplier;
import me.jetby.libb.gui.parser.view.ViewRequirement;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

public class ParsedGui {

    @Getter
    private final AdvancedGui gui;
    private final ActionBlock onOpen;
    private final ActionBlock onClose;
    @Getter
    private final List<String> openCommands;
    private final Player player;


    private final Map<String, ItemEntry> itemEntries = new LinkedHashMap<>();

    private record ItemEntry(Item item, ItemWrapper wrapper) {
    }

    public ParsedGui(@NotNull Player player, Gui gui) {
        this.player = player;
        this.openCommands = gui.command();
        this.onOpen = gui.onOpen();
        this.onClose = gui.onClose();

        this.gui = new AdvancedGui(
                Libb.MINI_MESSAGE.deserialize(
                        PlaceholderAPI.setPlaceholders(player, gui.title())),
                gui.size());

        attachLifecycle();
        registerItems(gui.items());
    }

    public ParsedGui(Player player, FileConfiguration configuration) {
        this.player = player;

        String title = configuration.getString("title");
        int size = configuration.getInt("size");

        this.openCommands = configuration.getStringList("command");
        this.onOpen = ParseUtil.getActionBlock(configuration, "on_open");
        this.onClose = ParseUtil.getActionBlock(configuration, "on_close");

        this.gui = new AdvancedGui(title, size);

        attachLifecycle();
        registerItems(ParseUtil.getItems(configuration));
    }

    private void attachLifecycle() {
        gui.onOpen(event -> {
            if (onOpen != null)
                ActionExecute.run(ActionContext.of(player), onOpen);

            applyViewRequirements();
        });

        gui.onClose(event -> {
            if (onClose != null)
                ActionExecute.run(ActionContext.of(player), onClose);
        });
    }

    /**
     * Evaluates each item's {@code view_requirements} and applies any matching
     * patches.  Call this any time you want to refresh the GUI (e.g. after a
     * command or timer updates a placeholder value).
     */
    public void applyViewRequirements() {
        for (ItemEntry entry : itemEntries.values()) {
            applyViewRequirements(entry.item(), entry.wrapper());
        }
    }

    /**
     * Overload that accepts an explicit player — useful when the original
     * player reference may be stale (e.g. after a reconnect).
     */
    public void applyViewRequirements(Player viewer) {
        for (ItemEntry entry : itemEntries.values()) {
            applyViewRequirementsFor(entry.item(), entry.wrapper(), viewer);
        }
    }

    private void applyViewRequirements(Item item, ItemWrapper wrapper) {
        applyViewRequirementsFor(item, wrapper, player);
    }

    private void applyViewRequirementsFor(Item item, ItemWrapper wrapper, Player viewer) {
        List<ViewRequirement> reqs = item.viewRequirements();
        if (reqs == null || reqs.isEmpty()) return;

        for (ViewRequirement req : reqs) {
            String resolved = PlaceholderAPI.setPlaceholders(viewer, req.expression());

            if (evaluateExpression(resolved)) {
                ItemPatchApplier.apply(wrapper, req.patches(), viewer, gui);
            }
        }
    }

    /**
     * Tiny expression evaluator that handles the common cases used in configs:
     * {@code A > B}, {@code A < B}, {@code A >= B}, {@code A <= B},
     * {@code A == B}, {@code A != B}, and a bare boolean / truthy string.
     *
     * <p>Both sides are first tried as doubles; if that fails they are compared
     * as strings.
     */
    static boolean evaluateExpression(String expression) {
        if (expression == null) return false;
        expression = expression.trim();

        for (String op : new String[]{">=", "<=", "!=", "==", ">", "<"}) {
            int idx = expression.indexOf(op);
            if (idx < 0) continue;

            String left = expression.substring(0, idx).trim();
            String right = expression.substring(idx + op.length()).trim();

            try {
                double l = Double.parseDouble(left);
                double r = Double.parseDouble(right);
                return switch (op) {
                    case ">" -> l > r;
                    case "<" -> l < r;
                    case ">=" -> l >= r;
                    case "<=" -> l <= r;
                    case "==" -> l == r;
                    case "!=" -> l != r;
                    default -> false;
                };
            } catch (NumberFormatException ignored) {
                int cmp = left.compareTo(right);
                return switch (op) {
                    case ">" -> cmp > 0;
                    case "<" -> cmp < 0;
                    case ">=" -> cmp >= 0;
                    case "<=" -> cmp <= 0;
                    case "==" -> cmp == 0;
                    case "!=" -> cmp != 0;
                    default -> false;
                };
            }
        }

        return Boolean.parseBoolean(expression) ||
                expression.equalsIgnoreCase("yes") ||
                expression.equals("1");
    }

    private void registerItems(List<Item> items) {
        if (items == null) return;

        for (Item item : items) {
            if (item.itemStack() == null) continue;

            String key = UUID.randomUUID().toString();
            ItemWrapper wrapper = new ItemWrapper(item.itemStack());

            wrapper.slots(item.slots().toArray(new Integer[0]));

            if (item.displayName() != null)
                wrapper.setDisplayName(PlaceholderAPI.setPlaceholders(player, item.displayName()));

            wrapper.setRawDisplayName(item.displayName());
            wrapper.setRawLore(item.lore());

            List<String> lore = new ArrayList<>();
            for (String line : item.lore())
                lore.add(PlaceholderAPI.setPlaceholders(player, line));
            wrapper.setLore(lore);

            if (item.flags() != null)
                wrapper.flags(item.flags().toArray(new ItemFlag[0]));

            wrapper.onClick(event -> {
                event.setCancelled(true);
                Player p = (Player) event.getWhoClicked();

                if (item.onClick().containsKey(null))
                    ActionExecute.run(ActionContext.of(p)
                                    .with(wrapper)
                                    .with(gui)
                                    .with(ParsedGui.class, this),
                            item.onClick().get(null));

                for (var entry : item.onClick().entrySet()) {
                    ClickType required = entry.getKey();
                    if (required == null) continue;
                    if (!event.getClick().equals(required)) continue;
                    ActionExecute.run(ActionContext.of(p)
                                    .with(wrapper)
                                    .with(gui)
                                    .with(ParsedGui.class, this),
                            entry.getValue());
                }

                for (var entry : itemHandlers.entrySet()) {
                    String handlerKey = entry.getKey();
                    if (item.section() != null && item.section().contains(handlerKey)) {
                        entry.getValue().accept(event, item.section());
                    }
                }
            });

            gui.setItem(key, wrapper);
            itemEntries.put(key, new ItemEntry(item, wrapper));
        }
    }

    private final Map<String, BiConsumer<InventoryClickEvent, ConfigurationSection>> itemHandlers = new HashMap<>();

    public ParsedGui setItemHandler(String key,
                                    BiConsumer<InventoryClickEvent, ConfigurationSection> handler) {
        itemHandlers.put(key, handler);
        return this;
    }
}