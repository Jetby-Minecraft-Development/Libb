package me.jetby.libb.gui;

import lombok.Getter;
import me.jetby.libb.Keys;
import me.jetby.libb.gui.item.ItemWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class AdvancedGui implements InventoryHolder {

    private final Inventory inventory;
    @Getter
    private final Map<String, ItemWrapper> wrappers = new HashMap<>();
    private Consumer<InventoryClickEvent> onClick;
    private Consumer<InventoryDragEvent> onDrag;
    private Consumer<InventoryOpenEvent> onOpen;
    private Consumer<InventoryCloseEvent> onClose;
    @Getter
    private boolean isLockEmptySlots = true;

    public void lockEmptySlots(boolean cancel) {
        this.isLockEmptySlots = cancel;
    }

    public Player player;

    private final Map<String, Function<Player, String>> placeholders = new LinkedHashMap<>();


    public AdvancedGui(@NotNull Inventory inventory) {
        this.inventory = inventory;
    }

    public AdvancedGui(String title) {
        this.inventory = Bukkit.createInventory(this, InventoryType.CHEST, title);
    }

    public AdvancedGui(String title, int size) {
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    public AdvancedGui(String title, InventoryType inventoryType) {
        this.inventory = Bukkit.createInventory(this, inventoryType, title);
    }

    public AdvancedGui(Component title) {
        this.inventory = Bukkit.createInventory(this, InventoryType.CHEST, title);
    }

    public AdvancedGui(Component title, int size) {
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    public AdvancedGui(Component title, @NotNull InventoryType inventoryType) {
        this.inventory = Bukkit.createInventory(this, inventoryType, title);
    }



    public void setItem(@NotNull String key, @NotNull ItemWrapper wrapper) {
        if (wrapper.slots() == null) return;

        ItemStack itemStack = wrapper.itemStack();
        if (itemStack == null) {
            itemStack = new ItemStack(wrapper.material());
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                meta.displayName(wrapper.displayName());
                meta.lore(wrapper.lore());
                meta.setCustomModelData(wrapper.customModelData());
                if (wrapper.enchanted()) {
                    meta.addEnchant(Enchantment.OXYGEN, 1, false);
                }
                if (wrapper.flags() != null && !wrapper.flags().isEmpty()) {
                    for (ItemFlag flag : wrapper.flags()) {
                        meta.addItemFlags(flag);
                    }
                }
                meta.getPersistentDataContainer().set(Keys.GUI_ITEM, PersistentDataType.STRING, key);
                itemStack.setItemMeta(meta);
            }
            wrapper.itemStack(itemStack);
        }

        ItemMeta meta = itemStack.getItemMeta();
        meta.getPersistentDataContainer().set(Keys.GUI_ITEM, PersistentDataType.STRING, key);
        itemStack.setItemMeta(meta);

        for (int slot : wrapper.slots()) {
            inventory.setItem(slot, itemStack);
        }

        wrappers.put(key, wrapper);

    }

    public void open(@NotNull Player player) {
        this.player = player;
        player.openInventory(inventory);
    }

    public InventoryHolder getHolder() {
        return this;
    }

    @Nullable
    public Consumer<InventoryClickEvent> onClick() {
        return onClick;
    }

    @Nullable
    public Consumer<InventoryDragEvent> onDrag() {
        return onDrag;
    }

    @Nullable
    public Consumer<InventoryOpenEvent> onOpen() {
        return onOpen;
    }

    @Nullable
    public Consumer<InventoryCloseEvent> onClose() {
        return onClose;
    }

    public void onClick(Consumer<InventoryClickEvent> event) {
        this.onClick = event;
    }

    public void onDrag(Consumer<InventoryDragEvent> event) {
        this.onDrag = event;
    }

    public void onOpen(Consumer<InventoryOpenEvent> event) {
        this.onOpen = event;
    }

    public void onClose(Consumer<InventoryCloseEvent> event) {
        this.onClose = event;
    }


    public void updateItem(@NotNull String key) {
        ItemWrapper wrapper = wrappers.get(key);
        if (wrapper == null || wrapper.slots() == null) return;
        for (int slot : wrapper.slots()) {
            inventory.setItem(slot, wrapper.itemStack());
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

}
