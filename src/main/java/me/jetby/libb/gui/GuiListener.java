package me.jetby.libb.gui;

import me.jetby.libb.Keys;
import me.jetby.libb.gui.item.ItemWrapper;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public class GuiListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        AdvancedGui gui = getHolder(e.getInventory());
        if (gui == null) return;

        if (gui.onClick()!=null) {
            gui.onClick().accept(e);
        }
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= e.getInventory().getSize()) return;

        ItemStack itemStack = e.getCurrentItem();
        if (itemStack == null) return;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(Keys.GUI_ITEM)) {
            return;
        }
        String key = meta.getPersistentDataContainer().get(Keys.GUI_ITEM, PersistentDataType.STRING);
        ItemWrapper wrapper = gui.getWrappers().get(key);
        if (wrapper == null) {
            return;
        }
        if (wrapper.onClick() != null) {
            wrapper.onClick().accept(e);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        AdvancedGui gui = getHolder(e.getInventory());
        if (gui == null) return;

        if (gui.onDrag()!=null) {
            gui.onDrag().accept(e);
        }

    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        AdvancedGui gui = getHolder(e.getInventory());
        if (gui == null) return;

        if (gui.onOpen()!=null) {
            gui.onOpen().accept(e);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        AdvancedGui gui = getHolder(e.getInventory());
        if (gui == null) return;

        if (gui.onClose()!=null) {
            gui.onClose().accept(e);
        }
    }

    @Nullable
    private static AdvancedGui getHolder(Inventory inventory) {
        if (inventory == null) return null;

        InventoryHolder holder = inventory.getHolder();
        if (holder == null) return null;

        return holder instanceof AdvancedGui ? ((AdvancedGui) holder) : null;
    }

}
