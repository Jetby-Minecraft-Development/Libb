package me.jetby.libb.gui.parser;

import lombok.Getter;
import me.jetby.libb.gui.item.ItemWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ConfigurableClickEvent extends InventoryClickEvent {
    @Getter
    private final ConfigurationSection section;
    @Getter
    private final ItemWrapper wrapper;

    public ConfigurableClickEvent(InventoryClickEvent event, ConfigurationSection section, ItemWrapper wrapper) {
        super(event.getView(), event.getSlotType(), event.getSlot(), event.getClick(), event.getAction());
        this.section = section;
        this.wrapper = wrapper;
    }
}
