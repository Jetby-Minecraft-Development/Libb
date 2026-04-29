package me.jetby.libb.gui.item;

import me.jetby.libb.color.Serializer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ItemWrapper {

    @Nullable
    private String key;

    private List<Integer> slots;
    private ItemStack itemStack;
    private Consumer<InventoryClickEvent> onClick;
    private Serializer serializer;


    public Serializer serializer() {
        return serializer;
    }

    public void serializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public ItemWrapper(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemWrapper(@NotNull Material material) {
        this.itemStack = new ItemStack(material);
    }

    public ItemWrapper(@NotNull Material material, int amount) {
        this.itemStack = new ItemStack(material, amount);
    }

    private void applyMeta(Consumer<ItemMeta> editor) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;
        editor.accept(meta);
        itemStack.setItemMeta(meta);
    }

    public Consumer<InventoryClickEvent> onClick() {
        return onClick;
    }

    public void onClick(Consumer<InventoryClickEvent> onClick) {
        this.onClick = onClick;
    }

    public List<Integer> slots() {
        return slots;
    }

    public void slots(@Range(from = 0, to = 54) Integer... slot) {
        this.slots = Arrays.asList(slot);
    }

    public ItemStack itemStack() {
        return itemStack;
    }

    public void itemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public Material material() {
        return itemStack.getType();
    }

    public void material(Material material) {
        itemStack.setType(material);
    }

    public Component displayName() {
        ItemMeta meta = itemStack.getItemMeta();
        return meta != null ? meta.displayName() : null;
    }

    public void displayName(Component displayName) {
        applyMeta(meta -> meta.displayName(displayName));
    }

    public void setDisplayName(String text) {
        displayName(serializer == null ? Component.text(text) : serializer.deserialize(text));
    }

    public void displayName(String text) {
        setDisplayName(text);
    }

    public List<Component> lore() {
        ItemMeta meta = itemStack.getItemMeta();
        return meta != null ? meta.lore() : null;
    }

    public void lore(List<Component> lore) {
        applyMeta(meta -> meta.lore(lore));
    }

    public void setLore(String... lines) {
        List<Component> list = new ArrayList<>();
        for (String line : lines) {
            list.add(serializer == null ? Component.text(line) : serializer.deserialize(line));
        }
        lore(list);
    }

    public void setLore(List<String> lines) {
        List<Component> list = new ArrayList<>();
        for (String line : lines) {
            list.add(serializer == null ? Component.text(line) : serializer.deserialize(line));
        }
        lore(list);
    }

    public int customModelData() {
        ItemMeta meta = itemStack.getItemMeta();
        return meta != null && meta.hasCustomModelData() ? meta.getCustomModelData() : 0;
    }

    public void customModelData(int customModelData) {
        applyMeta(meta -> meta.setCustomModelData(customModelData));
    }

    public boolean enchanted() {
        ItemMeta meta = itemStack.getItemMeta();
        return meta != null && meta.hasEnchants();
    }

    public void enchanted(boolean enchanted) {
        if (enchanted) {
            applyMeta(meta -> {
                meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            });
        }
    }

    public List<ItemFlag> flags() {
        ItemMeta meta = itemStack.getItemMeta();
        return meta != null ? new ArrayList<>(meta.getItemFlags()) : new ArrayList<>();
    }

    public void flags(ItemFlag... flags) {
        applyMeta(meta -> meta.addItemFlags(flags));
    }

    public int amount() {
        return itemStack.getAmount();
    }

    public void amount(int amount) {
        itemStack.setAmount(amount);
    }

    public static Builder builder(@NotNull Material material) {
        return new Builder(material);
    }

    public @Nullable String key() {
        return key;
    }

    public void key(@Nullable String key) {
        this.key = key;
    }

    public static class Builder {
        private final Material material;
        private ItemStack itemStack;
        private List<Integer> slots;
        private int amount = 1;
        private Component displayName;
        private List<Component> lore;
        private int customModelData;
        private boolean enchanted;
        private List<ItemFlag> flags;
        private Consumer<InventoryClickEvent> onClick;
        private Serializer serializer;
        private String key;

        private Builder(@NotNull Material material) {
            this.material = material;
        }

        public Builder key(@Nullable String key) {
            this.key = key;
            return this;
        }

        public Builder serializer(Serializer serializer) {
            this.serializer = serializer;
            return this;
        }

        public Builder itemStack(ItemStack itemStack) {
            this.itemStack = itemStack;
            return this;
        }

        public Builder slots(@Range(from = 0, to = 54) Integer... slot) {
            this.slots = Arrays.asList(slot);
            return this;
        }

        public Builder displayName(Component displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder setDisplayName(String text) {
            displayName(serializer == null ? Component.text(text) : serializer.deserialize(text));
            return this;
        }

        public Builder lore(List<Component> lore) {
            this.lore = lore;
            return this;
        }

        public Builder setLore(List<String> lines) {
            List<Component> list = new ArrayList<>();
            for (String line : lines) {
                list.add(serializer == null ? Component.text(line) : serializer.deserialize(line));
            }
            this.lore = list;
            return this;
        }

        public Builder customModelData(int customModelData) {
            this.customModelData = customModelData;
            return this;
        }

        public Builder enchanted(boolean enchanted) {
            this.enchanted = enchanted;
            return this;
        }

        public Builder flags(ItemFlag... flags) {
            this.flags = Arrays.asList(flags);
            return this;
        }

        public Builder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public Builder onClick(Consumer<InventoryClickEvent> onClick) {
            this.onClick = onClick;
            return this;
        }

        public ItemWrapper build() {
            ItemWrapper wrapper = new ItemWrapper(
                    itemStack != null ? itemStack : new ItemStack(material, amount)
            );

            wrapper.key(key);
            wrapper.serializer(serializer);

            wrapper.slots = slots;
            wrapper.onClick = onClick;

            if (displayName != null) wrapper.displayName(displayName);
            if (lore != null) wrapper.lore(lore);
            if (customModelData != 0) wrapper.customModelData(customModelData);
            if (enchanted) wrapper.enchanted(true);
            if (flags != null) wrapper.flags(flags.toArray(new ItemFlag[0]));

            return wrapper;
        }
    }
}