package org.jetby.libb.gui;

import lombok.Getter;
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
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetby.libb.AdventureReflect;
import org.jetby.libb.InstanceFactory;
import org.jetby.libb.LibbApi;
import org.jetby.libb.color.Serializer;
import org.jetby.libb.gui.item.ItemWrapper;
import org.jetby.libb.platform.Platform;
import org.jetby.libb.platform.PlatformMeta;
import org.jetby.libb.util.CustomModelDataUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
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
    private boolean isLockEmptySlots = false;

    public Serializer defaultSerializer = null;

    public void lockEmptySlots(boolean cancel) {
        this.isLockEmptySlots = cancel;
    }

    public Player player;

    private static Inventory createInventory(InventoryHolder holder, int size, Component title) {
        if (LibbApi.Settings.PLATFORM == Platform.PAPER) {
            return Bukkit.createInventory(holder, size, title);
        }
        return Bukkit.createInventory(holder, size, AdventureReflect.toLegacySection(title));
    }

    private static Inventory createInventory(InventoryHolder holder, InventoryType type, Component title) {
        if (LibbApi.Settings.PLATFORM == Platform.PAPER) {
            return Bukkit.createInventory(holder, type, title);
        }
        return Bukkit.createInventory(holder, type, AdventureReflect.toLegacySection(title));
    }

    public AdvancedGui(@NotNull Inventory inventory) {
        this.inventory = inventory;
    }

    public AdvancedGui(String title) {
        this.inventory = Bukkit.createInventory(this, InventoryType.CHEST, title);
    }

    public AdvancedGui(String title, int size) {
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    public AdvancedGui(String title, Serializer serializer) {
        this.defaultSerializer = serializer;
        this.inventory = createInventory(this, InventoryType.CHEST, defaultSerializer.deserialize(title));
    }

    public AdvancedGui(String title, int size, Serializer serializer) {
        this.defaultSerializer = serializer;
        this.inventory = createInventory(this, size, defaultSerializer.deserialize(title));
    }

    public AdvancedGui(String title, InventoryType inventoryType) {
        this.inventory = Bukkit.createInventory(this, inventoryType, title);
    }

    public AdvancedGui(Component title) {
        this.inventory = createInventory(this, InventoryType.CHEST, title);
    }

    public AdvancedGui(Component title, int size) {
        this.inventory = createInventory(this, size, title);
    }

    public AdvancedGui(Component title, @NotNull InventoryType inventoryType) {
        this.inventory = createInventory(this, inventoryType, title);
    }

    @Nullable
    public ItemWrapper getItem(@NotNull String key) {
        return wrappers.get(key);
    }

    @Nullable
    public ItemWrapper getItemBySlot(int slot) {
        return wrappers.values().stream()
                .filter(w -> w.slots().contains(slot))
                .findFirst()
                .orElse(null);
    }

    public void setItem(@NotNull String key, @NotNull ItemWrapper wrapper) {
        if (wrapper.slots() == null) return;
        if (wrapper.key() == null) wrapper.key(key);
        if (wrapper.serializer() == null) wrapper.serializer(defaultSerializer);

        ItemStack itemStack = wrapper.itemStack();
        if (itemStack == null) {
            itemStack = new ItemStack(wrapper.material());
            wrapper.itemStack(itemStack);
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {

            PlatformMeta.setDisplayName(meta, wrapper.displayName());
            PlatformMeta.setLore(meta, wrapper.lore());

            CustomModelDataUtil.apply(meta, wrapper.customModelDataComponent());

            for (Map.Entry<Enchantment, Integer> entry : wrapper.enchantments().entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }


            if (wrapper.enchanted()) {
                meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
            }
            if (wrapper.flags() != null && !wrapper.flags().isEmpty()) {
                for (ItemFlag flag : wrapper.flags()) {
                    meta.addItemFlags(flag);
                }
            }
            meta.getPersistentDataContainer().set(InstanceFactory.GUI_ITEM, PersistentDataType.STRING, key);
            itemStack.setItemMeta(meta);
        }

        for (int slot : wrapper.slots()) {
            if (slot<0 || slot> 53) continue;
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