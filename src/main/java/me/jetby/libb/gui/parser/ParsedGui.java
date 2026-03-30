package me.jetby.libb.gui.parser;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.jetby.libb.Libb;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionExecute;
import me.jetby.libb.action.record.ActionBlock;
import me.jetby.libb.gui.AdvancedGui;
import me.jetby.libb.gui.item.ItemWrapper;
import me.jetby.libb.gui.parser.view.RequirementEvaluator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║                        ParsedGui                                 ║
 * ║       "A GUI that reads itself from config and just works."      ║
 * ╚══════════════════════════════════════════════════════════════════╝
 *
 * <p>ParsedGui is the main class you interact with when opening inventory GUIs.
 * It takes a {@link Gui} definition (loaded from YAML config) and turns it into
 * a real Bukkit inventory — complete with items, click actions, open/close hooks,
 * placeholder support, and slot priority logic.</p>
 *
 * <h2>How it fits into the system</h2>
 * <pre>
 *   YAML config file
 *       ↓  (parsed by ParseUtil / FileConfiguration)
 *   Gui record  ──────────────────────────────┐
 *       ↓  (passed into constructor)          │
 *   ParsedGui  ←─── extends AdvancedGui       │  ← you're here
 *       ↓  (opens it)                         │
 *   Player sees inventory                     │
 *                                             │
 *   Items inside the Gui record ──────────────┘
 *       (each Item has slots, display name, lore, click actions…)
 * </pre>
 *
 * <h2>Basic usage (from another plugin)</h2>
 * <pre>{@code
 * // Open a GUI defined in config for a player:
 * Gui guiDefinition = ...; // loaded from your YAML
 * ParsedGui gui = new ParsedGui(player, guiDefinition, myPlugin);
 * gui.open(player);
 *
 * // OR from a FileConfiguration directly:
 * ParsedGui gui = new ParsedGui(player, plugin.getConfig(), myPlugin);
 * gui.open(player);
 * }</pre>
 *
 * <h2>Adding runtime placeholders</h2>
 * <pre>{@code
 * // Replace {price} everywhere in display names, lore, and action lines:
 * gui.setReplace("{price}", "500");
 * // IMPORTANT: call setReplace() BEFORE open(), otherwise items are already built.
 * }</pre>
 *
 * <h2>Reacting to item clicks from code (click handlers)</h2>
 * <pre>{@code
 * // "sell_button" is the section key of the item in YAML
 * gui.addClickHandler("sell_button", event -> {
 *     Player clicker = (Player) event.getWhoClicked();
 *     clicker.sendMessage("You clicked the sell button!");
 * });
 * }</pre>
 *
 * <h2>Refreshing the GUI</h2>
 * <pre>{@code
 * // Clears and rebuilds all items (re-evaluates view_requirements, re-applies placeholders):
 * gui.refresh();
 * }</pre>
 */
@Getter
public class ParsedGui extends AdvancedGui {

    // ─────────────────────────────────────────────────────────────────────────
    // Fields
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * The player this GUI was opened for. Used for PlaceholderAPI and action context.
     */
    private final Player viewer;

    /**
     * The parsed GUI definition — title, size, items, open/close actions, etc.
     * This is the "blueprint" that ParsedGui renders from.
     */
    private final Gui gui;

    /**
     * Programmatic click handlers registered by your plugin code (not from config).
     * Key = item section key (e.g. "sell_button"), Value = your Consumer.
     *
     * <p>These run IN ADDITION to whatever on_click actions are defined in YAML.
     * Use {@link #addClickHandler(String, Consumer)} to add them.</p>
     */
    private final Map<String, Consumer<ConfigurableClickEvent>> clickHandlers = new HashMap<>();

    /**
     * Runtime string replacements applied to all display names, lore, and action lines.
     * Populated via {@link #setReplace(String, String)}.
     *
     * <p>Example entry: {@code "{price}" → "500"}</p>
     */
    private final Map<String, String> placeholders = new HashMap<>();

    /**
     * The plugin that "owns" this GUI instance.
     * Passed to {@link ActionContext} so that action resolution knows which namespace
     * to prefer (e.g. {@code [sell_all]} resolves to {@code [treexbuyer:sell_all]}).
     */
    private final JavaPlugin plugin;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Create and prepare a GUI from a pre-parsed {@link Gui} record.
     *
     * <p>This is the preferred constructor — parse your config once with
     * {@link ParseUtil#getItems(FileConfiguration)} and reuse the {@link Gui} object.</p>
     *
     * @param viewer        the player who will see this GUI
     * @param guiDefinition the parsed GUI blueprint (title, size, items, actions…)
     * @param plugin        your plugin — needed for action namespace resolution
     */
    public ParsedGui(@NotNull Player viewer, @NotNull Gui guiDefinition, JavaPlugin plugin) {
        // AdvancedGui needs the title (as a Component) and the inventory size
        super(Libb.MINI_MESSAGE.deserialize(guiDefinition.title()), guiDefinition.size());
        this.gui = guiDefinition;
        this.viewer = viewer;
        this.plugin = plugin;

        // Wire up open/close/click lifecycle events, then fill the inventory
        setupLifecycleListeners();
        buildItems(guiDefinition.items());
    }

    /**
     * Create and prepare a GUI directly from a {@link FileConfiguration}.
     *
     * <p>Use this when you don't want to pre-parse — just pass in the raw config section
     * and ParsedGui will do everything inline. Slightly less efficient if you open the
     * same GUI for many players (you'd re-parse each time).</p>
     *
     * @param viewer the player who will see this GUI
     * @param config the config section with title, size, Items, on_open, on_close…
     * @param plugin your plugin — needed for action namespace resolution
     */
    public ParsedGui(@NotNull Player viewer, @NotNull FileConfiguration config, JavaPlugin plugin) {
        super(Libb.MINI_MESSAGE.deserialize(config.getString("title", "")), config.getInt("size", 54));
        this.viewer = viewer;
        this.plugin = plugin;

        // Build the Gui record inline from raw config values
        this.gui = new Gui(
                config.getString("id"),
                applyPlaceholders(config.getString("title")),
                config.getInt("size"),
                config.getStringList("command"),
                applyPlaceholders(config.getStringList("pre_open")),
                ParseUtil.getActionBlock(config, "on_open"),
                ParseUtil.getActionBlock(config, "on_close"),
                ParseUtil.getItems(config)
        );
        setupLifecycleListeners();
        buildItems(gui.items());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Wires the three inventory lifecycle hooks: click, open, close.
     *
     * <p>Called once from the constructor. You don't need to call this yourself.</p>
     *
     * <ul>
     *   <li><b>onClick</b> — cancels the event globally (prevents item theft from GUI)</li>
     *   <li><b>onOpen</b>  — refreshes items and runs the YAML {@code on_open} action block</li>
     *   <li><b>onClose</b> — runs the YAML {@code on_close} action block</li>
     * </ul>
     */
    public void setupLifecycleListeners() {
        // Cancel all clicks by default — items can't be taken out of the GUI
        onClick(event -> {
            event.setCancelled(true);
        });

        onOpen(event -> {
            // Always rebuild items on open so view_requirements are re-evaluated
            // and placeholders are current
            refresh();
            if (gui.onOpen() != null)
                ActionExecute.run(ActionContext.of(viewer, plugin)
                        .replaceFromMap(placeholders)
                        .with(this), gui.onOpen());
        });

        onClose(event -> {
            if (gui.onClose() != null)
                ActionExecute.run(ActionContext.of(viewer, plugin)
                        .replaceFromMap(placeholders)
                        .with(this), gui.onClose());
        });
    }

    /**
     * Clears the inventory and rebuilds all items from the GUI definition.
     *
     * <p>Call this whenever you want the GUI to reflect updated state — for example,
     * after a purchase changes a player's balance and item visibility should update.</p>
     *
     * <pre>{@code
     * // Inside a click handler:
     * gui.refresh();
     * }</pre>
     */
    public void refresh() {
        clearInventory();
        buildItems(gui.items());
    }

    /**
     * Removes all currently displayed ItemWrapper items from the inventory.
     * Used internally by {@link #refresh()} before rebuilding.
     */
    public void clearInventory() {
        getWrappers().forEach((string, wrapper) -> {
            getInventory().removeItemAnySlot(wrapper.itemStack());
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Item Building
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * The core rendering method — turns a list of {@link Item} definitions into
     * actual inventory items with click handlers.
     *
     * <h3>What it does, step by step</h3>
     * <ol>
     *   <li>Groups all items by slot number (multiple items can target the same slot)</li>
     *   <li>Sorts candidates by {@link Item#priority()} — lower number = higher priority</li>
     *   <li>Picks the first candidate whose {@code view_requirements} pass for this player</li>
     *   <li>Builds an {@link ItemWrapper} for each winner and registers it in the inventory</li>
     * </ol>
     *
     * <h3>Priority / view_requirements example (YAML)</h3>
     * <pre>
     * Items:
     *   button_locked:
     *     material: RED_STAINED_GLASS_PANE
     *     slot: 13
     *     priority: 1                         # shown first if requirement fails
     *     view_requirements:
     *       - "%vault_eco_balance% < 100"      # visible only when balance < 100
     *   button_unlocked:
     *     material: EMERALD
     *     slot: 13
     *     priority: 2                         # fallback if balance >= 100
     * </pre>
     *
     * @param items list of item definitions from the Gui record; no-op if null
     */
    public void buildItems(List<Item> items) {
        if (items == null) return;

        // Step 1: group all items that want each slot
        Map<Integer, List<Item>> slotCandidates = new LinkedHashMap<>();
        for (Item item : items) {
            if (item.itemStack() == null) continue;
            for (int slot : item.slots()) {
                slotCandidates.computeIfAbsent(slot, k -> new ArrayList<>()).add(item);
            }
        }

        // Step 2 & 3: for each slot, pick the highest-priority item whose requirements pass
        Map<Integer, Item> slotWinners = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<Item>> entry : slotCandidates.entrySet()) {
            int slot = entry.getKey();
            List<Item> sorted = new ArrayList<>(entry.getValue());
            sorted.sort(Comparator.comparingInt(Item::priority));  // ascending: 0 beats MAX_VALUE
            for (Item candidate : sorted) {
                if (RequirementEvaluator.meetsAll(viewer, candidate.viewRequirements())) {
                    slotWinners.put(slot, candidate);
                    break;  // found winner for this slot, stop
                }
            }
        }

        // An item can win multiple slots — group those back together so we create
        // one ItemWrapper (with multiple slot positions) instead of duplicates
        Map<Item, List<Integer>> itemWonSlots = new LinkedHashMap<>();
        for (Map.Entry<Integer, Item> e : slotWinners.entrySet()) {
            itemWonSlots.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
        }

        // Step 4: build and register each winner
        for (Map.Entry<Item, List<Integer>> e : itemWonSlots.entrySet()) {
            Item item = e.getKey();
            List<Integer> wonSlots = e.getValue();

            String key = UUID.randomUUID().toString();  // unique identifier for the wrapper map
            ItemWrapper wrapper = buildItemWrapper(item, wonSlots);
            setItem(key, wrapper);  // AdvancedGui.setItem places it into the inventory
        }
    }

    /**
     * Builds one {@link ItemWrapper} from an {@link Item} definition.
     * Applies display name, lore, enchant glow, flags, and the click handler.
     *
     * <p>The {@code wonSlots} list overrides the item's own slot list — used when the
     * same item definition wins several slots (e.g. a filler item spanning slots 0-8).</p>
     *
     * @param item     the item definition to render
     * @param wonSlots the exact slots to place this item in (after priority resolution)
     * @return a fully configured ItemWrapper ready for {@link AdvancedGui#setItem}
     */
    public ItemWrapper buildItemWrapper(Item item, List<Integer> wonSlots) {
        ItemWrapper wrapper = new ItemWrapper(item.itemStack());
        wrapper.slots(wonSlots.toArray(new Integer[0]));

        if (item.displayName() != null) {
            wrapper.displayName(applyPlaceholders(item.displayName()));
        }

        wrapper.setLore(applyPlaceholders(item.lore()));
        wrapper.enchanted(item.enchanted());

        if (item.flags() != null)
            wrapper.flags(item.flags().toArray(new ItemFlag[0]));

        // Wire up the click handler — cancels the event, then delegates to dispatchItemClick
        wrapper.onClick(event -> {
            event.setCancelled(true);
            Player clicker = (Player) event.getWhoClicked();
            dispatchItemClick(clicker, wrapper, item, event);
        });

        return wrapper;
    }

    /**
     * Convenience overload — uses the item's own slot list (no priority override).
     *
     * @param item the item to build
     * @return configured ItemWrapper
     */
    public ItemWrapper buildItemWrapper(Item item) {
        return buildItemWrapper(item, item.slots());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Click Dispatching
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Routes a click event to the correct action blocks and programmatic handlers.
     *
     * <p>Execution order for a single click:</p>
     * <ol>
     *   <li>Run the {@code on_click.any} action block (if defined in YAML) — fires for all click types</li>
     *   <li>Run the action block for the specific {@link ClickType} that matched (e.g. {@code on_click.left})</li>
     *   <li>Invoke all programmatic {@link #clickHandlers} whose key matches the item's section</li>
     * </ol>
     *
     * <h3>YAML example</h3>
     * <pre>
     * Items:
     *   my_button:
     *     on_click:
     *       any:
     *         - "[sound] UI_BUTTON_CLICK"   # plays on any click
     *       left:
     *         - "[message] Left clicked!"
     *       right:
     *         - "[message] Right clicked!"
     * </pre>
     *
     * @param clicker the player who clicked
     * @param wrapper the ItemWrapper that was clicked
     * @param item    the Item definition (has the action map and section)
     * @param event   the raw Bukkit click event
     */
    @SuppressWarnings("all")
    public void dispatchItemClick(@NotNull Player clicker, @NotNull ItemWrapper wrapper, @NotNull Item item,
                                  @NotNull InventoryClickEvent event) {

        // 1. "any" click — null key in the map means "runs for all click types"
        if (item.onClick().containsKey(null))
            ActionExecute.run(ActionContext.of(clicker, plugin)
                            .with(wrapper)
                            .replaceFromMap(placeholders)
                            .with(this),
                    item.onClick().get(null));

        // 2. Specific click type match (LEFT, RIGHT, SHIFT_LEFT, etc.)
        for (Map.Entry<ClickType, ActionBlock> entry : item.onClick().entrySet()) {
            ClickType requiredClick = entry.getKey();
            if (!event.getClick().equals(requiredClick)) continue;
            ActionExecute.run(ActionContext.of(clicker, plugin)
                            .replaceFromMap(placeholders)
                            .with(wrapper)
                            .with(this),
                    entry.getValue());
        }

        // 3. Programmatic handlers registered via addClickHandler()
        //    The item's section key must contain the handler's registered key
        for (Map.Entry<String, Consumer<ConfigurableClickEvent>> handlerEntry : clickHandlers.entrySet()) {
            if (item.section() != null && item.section().contains(handlerEntry.getKey()))
                handlerEntry.getValue().accept(new ConfigurableClickEvent(event, item.section(), wrapper));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Placeholder / String Replacement
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Register a runtime string replacement that will be applied to every display name,
     * lore line, and action line before they are rendered or executed.
     *
     * <p>Call this <b>before</b> {@link #open(Player)} (or before {@link #refresh()})
     * so that items are built with the replacements already applied.</p>
     *
     * <pre>{@code
     * gui.setReplace("{item_name}", "Diamond Sword")
     *    .setReplace("{price}", String.valueOf(price));
     * gui.open(player);
     * }</pre>
     *
     * @param key   the placeholder token, e.g. {@code "{price}"}
     * @param input the value to substitute in, e.g. {@code "500"}
     * @return {@code this} for method chaining
     */
    @SuppressWarnings("unused")
    public ParsedGui setReplace(String key, String input) {
        placeholders.put(key, input);
        return this;
    }

    /**
     * Apply all registered replacements + PlaceholderAPI to a single string.
     *
     * <p>Returns an empty string (not null) if {@code line} is null — safe to use
     * anywhere without null checks.</p>
     *
     * @param line the raw string (may contain {@code {tokens}} and {@code %papi_placeholders%})
     * @return the processed string
     */
    public String applyPlaceholders(String line) {
        if (line == null) return "";

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            line = line.replace(entry.getKey(), entry.getValue());
        }
        return PlaceholderAPI.setPlaceholders(viewer, line);
    }


    /**
     * Apply placeholders to every string in a list.
     *
     * @param lines raw strings
     * @return new list with all strings processed
     */
    public List<String> applyPlaceholders(List<String> lines) {
        List<String> result = new ArrayList<>(lines.size());
        for (String line : lines)
            result.add(applyPlaceholders(line));
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Programmatic API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Register a Java-side click handler for items with a matching section key.
     *
     * <p>This is how your plugin code reacts to clicks without putting logic in YAML.
     * The {@code sectionKey} must match the item's key inside the {@code Items:} block
     * in your config (e.g. if the item is under {@code Items.sell_button:}, use
     * {@code "sell_button"}).</p>
     *
     * <pre>{@code
     * gui.addClickHandler("sell_button", event -> {
     *     Player p = (Player) event.getWhoClicked();
     *     sellItems(p);
     *     gui.refresh();
     * });
     * }</pre>
     *
     * @param sectionKey the YAML item key to listen for
     * @param handler    your click logic
     * @return {@code this} for chaining
     */
    public ParsedGui addClickHandler(String sectionKey, Consumer<ConfigurableClickEvent> handler) {
        clickHandlers.put(sectionKey, handler);
        return this;
    }

    /**
     * Find all {@link Item} definitions whose section key contains the given string.
     *
     * <p>Useful when you need to iterate over a group of related items —
     * for example, all items tagged with {@code "upgrade_tier"} to apply bulk changes.</p>
     *
     * <pre>{@code
     * List<Item> upgradeItems = gui.getBySectionOption("upgrade_tier");
     * }</pre>
     *
     * @param sectionKey the key to filter by
     * @return a (possibly empty) list of matching items; never null
     */
    public List<Item> getBySectionOption(String sectionKey) {
        if (gui.items() == null) return new ArrayList<>();
        return gui.items().stream()
                .filter(item -> item.section() != null && item.section().contains(sectionKey))
                .toList();
    }
}