package me.jetby.libb.gui.parser;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.jetby.libb.Libb;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionExecute;
import me.jetby.libb.action.record.ActionBlock;
import me.jetby.libb.action.record.Expression;
import me.jetby.libb.gui.AdvancedGui;
import me.jetby.libb.gui.item.ItemWrapper;
import me.jetby.libb.gui.parser.view.RequirementEvaluator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

@Getter
public class ParsedGui extends AdvancedGui {

    private final Player viewer;
    private final Gui gui;
    private final Map<String, Consumer<ConfigurableClickEvent>> clickHandlers = new HashMap<>();
    private final Map<String, String> placeholders = new HashMap<>();

    public ParsedGui(@NotNull Player viewer, @NotNull Gui guiDefinition) {
        super(Libb.MINI_MESSAGE.deserialize(guiDefinition.title()), guiDefinition.size());
        this.gui = guiDefinition;
        this.viewer = viewer;

        setupLifecycleListeners();
        buildItems(guiDefinition.items());
    }

    public ParsedGui(@NotNull Player viewer, @NotNull FileConfiguration config) {
        super(Libb.MINI_MESSAGE.deserialize(config.getString("title", "")), config.getInt("size", 54));
        this.viewer = viewer;

        this.gui = new Gui(
                config.getString("id"),
                applyPlaceholders(config.getString("title")),
                config.getInt("size"),
                config.getStringList("command"),
                applyPlaceholders(config.getStringList("pre_open")),
                applyPlaceholders(ParseUtil.getActionBlock(config, "on_open")),
                applyPlaceholders(ParseUtil.getActionBlock(config, "on_close")),
                ParseUtil.getItems(config)
        );
        setupLifecycleListeners();
        buildItems(gui.items());
    }

    public void setupLifecycleListeners() {
        onClick(event -> {
            event.setCancelled(true);
        });
        onOpen(event -> {
            refresh();
            if (gui.onOpen() != null)
                ActionExecute.run(ActionContext.of(viewer)
                        .with(this), applyPlaceholders(gui.onOpen()));
        });
        onClose(event -> {
            if (gui.onClose() != null)
                ActionExecute.run(ActionContext.of(viewer)
                        .with(this), applyPlaceholders(gui.onClose()));
        });
    }

    public void refresh() {
        clearInventory();
        buildItems(gui.items());
    }

    public void clearInventory() {
        getWrappers().forEach((string, wrapper) -> {
            getInventory().removeItemAnySlot(wrapper.itemStack());
        });
    }

    public void buildItems(List<Item> items) {
        if (items == null) return;

        Map<Integer, List<Item>> slotCandidates = new LinkedHashMap<>();

        for (Item item : items) {
            if (item.itemStack() == null) continue;
            for (int slot : item.slots()) {
                slotCandidates.computeIfAbsent(slot, k -> new ArrayList<>()).add(item);
            }
        }

        Map<Integer, Item> slotWinners = new LinkedHashMap<>();

        for (Map.Entry<Integer, List<Item>> entry : slotCandidates.entrySet()) {
            int slot = entry.getKey();
            List<Item> sorted = new ArrayList<>(entry.getValue());
            sorted.sort(Comparator.comparingInt(Item::priority));
            for (Item candidate : sorted) {
                if (RequirementEvaluator.meetsAll(viewer, candidate.viewRequirements())) {
                    slotWinners.put(slot, candidate);
                    break;
                }
            }
        }

        Map<Item, List<Integer>> itemWonSlots = new LinkedHashMap<>();
        for (Map.Entry<Integer, Item> e : slotWinners.entrySet()) {
            itemWonSlots.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
        }

        for (Map.Entry<Item, List<Integer>> e : itemWonSlots.entrySet()) {
            Item item = e.getKey();
            List<Integer> wonSlots = e.getValue();

            String key = UUID.randomUUID().toString();
            ItemWrapper wrapper = buildItemWrapper(item, wonSlots);
            setItem(key, wrapper);
        }
    }

    public ItemWrapper buildItemWrapper(Item item, List<Integer> wonSlots) {
        ItemWrapper wrapper = new ItemWrapper(item.itemStack());
        wrapper.slots(wonSlots.toArray(new Integer[0]));

        if (item.displayName() != null) {
            wrapper.setDisplayName(applyPlaceholders(item.displayName()));
        }

        wrapper.setLore(applyPlaceholders(item.lore()));

        wrapper.enchanted(item.enchanted());

        if (item.flags() != null)
            wrapper.flags(item.flags().toArray(new ItemFlag[0]));

        wrapper.onClick(event -> {
            event.setCancelled(true);
            Player clicker = (Player) event.getWhoClicked();
            dispatchItemClick(clicker, wrapper, item, event);
        });

        return wrapper;
    }

    public ItemWrapper buildItemWrapper(Item item) {
        return buildItemWrapper(item, item.slots());
    }

    public void dispatchItemClick(Player clicker, ItemWrapper wrapper, Item item,
                                  org.bukkit.event.inventory.InventoryClickEvent event) {
        if (item.onClick().containsKey(null))

            ActionExecute.run(ActionContext.of(clicker)
                            .with(wrapper)
                            .with(this),
                    applyPlaceholders(item.onClick().get(null)));

        for (Map.Entry<ClickType, ActionBlock> entry : item.onClick().entrySet()) {
            ClickType requiredClick = entry.getKey();
            if (!event.getClick().equals(requiredClick)) continue;
            ActionExecute.run(ActionContext.of(clicker)
                            .with(wrapper)
                            .with(this),
                    applyPlaceholders(entry.getValue()));
        }

        for (Map.Entry<String, Consumer<ConfigurableClickEvent>> handlerEntry : clickHandlers.entrySet()) {
            if (item.section() != null && item.section().contains(handlerEntry.getKey()))
                handlerEntry.getValue().accept(new ConfigurableClickEvent(event, item.section(), wrapper));
        }
    }

    @SuppressWarnings("unused")
    public ParsedGui setReplace(String key, String input) {
        placeholders.put(key, input);
        return this;
    }

    public String applyPlaceholders(String line) {
        if (line == null) return "";

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            line = line.replace(entry.getKey(), entry.getValue());
        }
        return PlaceholderAPI.setPlaceholders(viewer, line);
    }

    public @Nullable ActionBlock applyPlaceholders(ActionBlock block) {
        if (block==null) return null;
        List<Expression> expressions = new ArrayList<>();
        for (Expression e : block.expressions()) {
            String expression = applyPlaceholders(e.input());
            List<String> success = applyPlaceholders(e.success());
            List<String> fail = applyPlaceholders(e.fail());
            expressions.add(new Expression(expression, success, fail));
        }
        return new ActionBlock(applyPlaceholders(block.staticActions()), expressions);
    }

    public List<String> applyPlaceholders(List<String> lines) {
        List<String> result = new ArrayList<>(lines.size());
        for (String line : lines)
            result.add(applyPlaceholders(line));
        return result;
    }

    public ParsedGui addClickHandler(String sectionKey, Consumer<ConfigurableClickEvent> handler) {
        clickHandlers.put(sectionKey, handler);
        return this;
    }
}