package me.jetby.libb.gui.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import me.jetby.libb.Libb;
import me.jetby.libb.gui.AdvancedGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ItemWrapper {
    private List<Integer> slots;
    private int amount;
    private ItemStack itemStack;
    private Material material;
    private Component displayName;
    private List<Component> lore;
    private int customModelData;
    private boolean enchanted;
    private List<ItemFlag> flags;
    private Consumer<InventoryClickEvent> onClick;

    public ItemWrapper(@NotNull final ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemWrapper(@NotNull final Material material) {
        this.material = material;
        this.itemStack = new ItemStack(material);
    }

    public ItemWrapper(@NotNull final Material material, int amount) {
        this.amount = amount;
        this.material = material;
        this.itemStack = new ItemStack(material, amount);
    }

    public final Consumer<InventoryClickEvent> onClick() {
        return onClick;
    }

    public final void onClick(Consumer<InventoryClickEvent> onClick) {
        this.onClick = onClick;
    }

    public final List<Integer> slots() {
        return slots;
    }

    public final void slots(Integer... slot) {
        this.slots = Arrays.asList(slot);
    }

    public final List<ItemFlag> flags() {
        return flags;
    }

    public final void flags(ItemFlag... flags) {
        this.flags = Arrays.asList(flags);
    }

    public final ItemStack itemStack() {
        return itemStack;
    }

    public final void itemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public final Material material() {
        return material;
    }

    public final void material(Material material) {
        this.material = material;
    }

    public final Component displayName() {
        return displayName;
    }

    public final void displayName(Component displayName) {
        this.displayName = displayName;
    }

    public final void setDisplayName(String displayName) {
        this.displayName = Libb.MINI_MESSAGE.deserialize("<italic:false>" + displayName);
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(this.displayName);
        itemStack.setItemMeta(meta);
    }

    public final List<Component> lore() {
        return lore;
    }

    public final void lore(List<Component> lore) {
        this.lore = lore;
        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(this.lore);
        itemStack.setItemMeta(meta);
    }

    public final void setLore(String... lore) {
        List<Component> list = new ArrayList<>();
        for (String line : lore) {
            list.add(Libb.MINI_MESSAGE.deserialize("<italic:false>" + line));
        }
        this.lore = list;
        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(this.lore);
        itemStack.setItemMeta(meta);
    }

    public final void setLore(List<String> lore) {
        List<Component> list = new ArrayList<>();
        for (String line : lore) {
            list.add(Libb.MINI_MESSAGE.deserialize("<italic:false>" + line));
        }
        this.lore = list;
        ItemMeta meta = itemStack.getItemMeta();
        meta.lore(this.lore);
        itemStack.setItemMeta(meta);
    }

    public final int customModelData() {
        return customModelData;
    }

    public final void customModelData(int customModelData) {
        this.customModelData = customModelData;
    }

    public final boolean enchanted() {
        return enchanted;
    }

    public final void enchanted(boolean enchanted) {
        this.enchanted = enchanted;
        if (enchanted) {
            ItemMeta meta = itemStack.getItemMeta();
            meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemStack.setItemMeta(meta);
        }
    }

    public final int amount() {
        return amount;
    }

    public final void amount(int amount) {
        this.amount = amount;
    }

    public static ItemWrapper.Builder builder(@NotNull Material material) {
        return new ItemWrapper.Builder(material);
    }

    public static class Builder {
        private List<Integer> slots;
        private int amount;
        private ItemStack itemStack;
        private Material material;
        private Component displayName;
        private List<Component> lore;
        private int customModelData;
        private boolean enchanted;
        private List<ItemFlag> flags;
        private Consumer<InventoryClickEvent> onClick;

        private Builder(@NotNull Material material) {
            this.material = material;
        }

        public ItemWrapper.Builder onClick(Consumer<InventoryClickEvent> onClick) {
            this.onClick = onClick;
            return this;
        }

        public final void flags(List<ItemFlag> flags) {
            this.flags = flags;
        }

        public ItemWrapper.Builder itemStack(ItemStack itemStack) {
            this.itemStack = itemStack;
            return this;
        }

        public ItemWrapper.Builder material(Material material) {
            this.material = material;
            return this;
        }

        public ItemWrapper.Builder slots(Integer... slot) {
            this.slots = Arrays.asList(slot);
            return this;
        }

        public ItemWrapper.Builder displayName(Component displayName) {
            this.displayName = displayName;
            return this;
        }

        public ItemWrapper.Builder lore(List<Component> lore) {
            this.lore = lore;
            return this;
        }

        public ItemWrapper.Builder customModelData(int customModelData) {
            this.customModelData = customModelData;
            return this;
        }

        public ItemWrapper.Builder enchanted(boolean enchanted) {
            this.enchanted = enchanted;
            return this;
        }

        public ItemWrapper.Builder amount(int amount) {
            this.amount = amount;
            return this;
        }


        public ItemWrapper build() {
            ItemWrapper wrapper = new ItemWrapper(material, amount);

            wrapper.displayName = displayName;
            wrapper.lore = lore;
            wrapper.slots = slots;
            wrapper.customModelData = customModelData;
            wrapper.enchanted = enchanted;
            wrapper.material = material;
            wrapper.itemStack = itemStack;
            wrapper.flags = flags;
            wrapper.onClick = onClick;

            return wrapper;
        }

    }

}
