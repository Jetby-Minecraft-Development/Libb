package me.jetby.libb.gui;

import me.jetby.libb.InstanceFactory;
import me.jetby.libb.gui.item.ItemWrapper;
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

        if (gui.onClick() != null) {
            gui.onClick().accept(e);
        }

        int slot = e.getRawSlot();
        if (slot < 0 || slot >= e.getInventory().getSize()) {
            if (gui.isLockEmptySlots() && e.isShiftClick()) e.setCancelled(true);
            return;
        }

        ItemStack itemStack = e.getCurrentItem();
        if (itemStack == null || itemStack.getType().isAir()) {
            if (gui.isLockEmptySlots()) e.setCancelled(true);
            return;
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(InstanceFactory.GUI_ITEM)) {
            if (gui.isLockEmptySlots()) e.setCancelled(true);
            return;
        }

        String key = meta.getPersistentDataContainer().get(InstanceFactory.GUI_ITEM, PersistentDataType.STRING);
        ItemWrapper wrapper = gui.getWrappers().get(key);
        if (wrapper == null) {
            if (gui.isLockEmptySlots()) e.setCancelled(true);
            return;
        }

        if (wrapper.onClick() != null) {
            wrapper.onClick().accept(e);
            gui.updateItem(key);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        AdvancedGui gui = getHolder(e.getInventory());
        if (gui == null) return;

        if (gui.onDrag() != null) {
            gui.onDrag().accept(e);
        } else if (gui.isLockEmptySlots()) {
            for (int slot : e.getRawSlots()) {
                if (slot < e.getInventory().getSize()) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent e) {
        AdvancedGui gui = getHolder(e.getInventory());
        if (gui == null) return;

        if (gui.onOpen() != null) {
            gui.onOpen().accept(e);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        AdvancedGui gui = getHolder(e.getInventory());
        if (gui == null) return;

        if (gui.onClose() != null) {
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
