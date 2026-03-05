package me.jetby.libb.gui.parser;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.jetby.libb.Libb;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionExecute;
import me.jetby.libb.action.record.ActionBlock;
import me.jetby.libb.gui.AdvancedGui;
import me.jetby.libb.gui.item.ItemWrapper;
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

    public ParsedGui(@NotNull Player player, Gui gui) {
        this.player = player;
        this.openCommands = gui.command();

        this.onOpen = gui.onOpen();
        this.onClose = gui.onClose();

        this.gui = new AdvancedGui(Libb.MINI_MESSAGE.deserialize(
                PlaceholderAPI.setPlaceholders(player, gui.title())),
                gui.size());

        this.gui.onOpen(event -> {
            if (onOpen == null) return;
            ActionExecute.run(ActionContext.of(player), onOpen);
        });
        this.gui.onClose(event -> {
            if (onClose == null) return;
            ActionExecute.run(ActionContext.of(player), onClose);
        });
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

        gui.onOpen(event -> {
            if (onOpen == null) return;
            ActionExecute.run(ActionContext.of(player), onOpen);
        });
        gui.onClose(event -> {
            if (onClose == null) return;
            ActionExecute.run(ActionContext.of(player), onClose);
        });

        registerItems(ParseUtil.getItems(configuration));

    }

    private void registerItems(List<Item> items) {
        if (items != null) {
            for (Item item : items) {
                String key = UUID.randomUUID().toString();
                if (item.itemStack() != null) {
                    ItemWrapper wrapper = new ItemWrapper(item.itemStack());
                    wrapper.slots(item.slots().toArray(new Integer[0]));
                    if (item.displayName() != null)
                        wrapper.setDisplayName(PlaceholderAPI.setPlaceholders(player, item.displayName()));

                    wrapper.setRawDisplayName(item.displayName());
                    wrapper.setRawLore(item.lore());

                    List<String> lore = new ArrayList<>();
                    for (String line : item.lore()) {
                        lore.add(PlaceholderAPI.setPlaceholders(player, line));
                    }
                    wrapper.setLore(lore);
                    if (item.flags() != null)
                        wrapper.flags(item.flags().toArray(new ItemFlag[0]));

                    wrapper.onClick(event -> {
                        event.setCancelled(true);
                        Player p = (Player) event.getWhoClicked();
                        // any click
                        if (item.onClick().containsKey(null))
                            ActionExecute.run(ActionContext.of(p)
                                    .with(wrapper)
                                    .with(gui), item.onClick().get(null));
                        // other click
                        for (var entry : item.onClick().entrySet()) {
                            ClickType required = entry.getKey();
                            ActionBlock block = entry.getValue();

                            if (!event.getClick().equals(required)) continue;

                            ActionExecute.run(ActionContext.of(p)
                                    .with(wrapper)
                                    .with(gui), block);
                        }
                        for (var entry : itemHandlers.entrySet()) {
                            String handlerKey = entry.getKey();
                            if (item.section().contains(handlerKey)) {
                                entry.getValue().accept(event, item.section());
                            }
                        }
                    });

                    gui.setItem(key, wrapper);
                }
            }
        }
    }

    private final Map<String, BiConsumer<InventoryClickEvent, ConfigurationSection>> itemHandlers = new HashMap<>();

    public ParsedGui setItemHandler(String key, BiConsumer<InventoryClickEvent, ConfigurationSection> handler) {
        itemHandlers.put(key, handler);
        return this;
    }

}
