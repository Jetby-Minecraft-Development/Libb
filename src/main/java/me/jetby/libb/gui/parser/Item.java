package me.jetby.libb.gui.parser;

import me.jetby.libb.action.record.ActionBlock;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Item {

    public Item(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
        ItemMeta meta = itemStack.getItemMeta();
        this.displayName = meta.getDisplayName();
        this.lore = meta.getLore();
        this.amount = itemStack.getAmount();
        this.material = itemStack.getType();
        this.customModelData = meta.getCustomModelData();
    }

    public @Nullable ItemStack itemStack() {
        return itemStack;
    }

    public void itemStack(@Nullable ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public int amount() {return amount;}

    public void amount(int amount) {this.amount = amount;}

    public @Nullable String type() {
        return type;
    }

    public void type(@Nullable String type) {
        this.type = type;
    }

    public @Nullable String displayName() {
        return displayName;
    }

    public void displayName(@Nullable String displayName) {
        this.displayName = displayName;
    }

    public @Nullable List<String> lore() {
        return lore;
    }

    public void lore(@Nullable List<String> lore) {
        this.lore = lore;
    }

    public @NotNull Material material() {
        return material;
    }

    public void material(@NotNull Material material) {
        this.material = material;
    }

    public @NotNull List<Integer> slots() {
        return slots;
    }

    public void slots(@NotNull List<Integer> slots) {
        this.slots = slots;
    }

    public @Nullable List<ItemFlag> flags() {
        return flags;
    }

    public void flags(@Nullable List<ItemFlag> flags) {
        this.flags = flags;
    }

    public @Nullable List<Enchantment> enchantments() {
        return enchantments;
    }

    public void enchantments(@Nullable List<Enchantment> enchantments) {
        this.enchantments = enchantments;
    }

    public @NotNull Map<ClickType, ActionBlock> onClick() {
        return onClick;
    }

    public void onClick(@NotNull Map<ClickType, ActionBlock> onClick) {
        this.onClick = onClick;
    }
    public int customModelData() {return customModelData;}
    public void customModelData(int customModelData) {this.customModelData = customModelData;}

    public @Nullable ConfigurationSection section() {
        return section;
    }

    public void section(@Nullable ConfigurationSection section) {
        this.section = section;
    }

    public @NotNull List<String> viewRequirements() {
        return viewRequirements;
    }

    public void viewRequirements(@NotNull List<String> viewRequirements) {
        this.viewRequirements = viewRequirements;
    }

    /**
     * Lower number = higher priority. Default is Integer.MAX_VALUE (lowest).
     */
    public int priority() {
        return priority;
    }

    public void priority(int priority) {
        this.priority = priority;
    }

    public boolean enchanted() {
        return enchanted;
    }

    public void enchanted(boolean enchanted) {
        this.enchanted = enchanted;
    }

    public Item(@NotNull Material material) {
        this.material = material;
        this.itemStack = new ItemStack(material);
    }

    public Item(@NotNull Material material, int amount) {
        this.material = material;
        this.amount = amount;
        this.itemStack = new ItemStack(material, amount);
    }

    private @Nullable ItemStack itemStack;
    private @Nullable String type;
    private @Nullable String displayName;
    private @Nullable List<String> lore;
    private @NotNull Material material = Material.STONE;
    private @NotNull List<Integer> slots = new ArrayList<>();
    private @Nullable List<ItemFlag> flags;
    private @Nullable List<Enchantment> enchantments;
    private @Nullable ConfigurationSection section;
    private @NotNull Map<ClickType, ActionBlock> onClick = new HashMap<>();
    private @NotNull List<String> viewRequirements = new ArrayList<>();
    private int priority = Integer.MAX_VALUE;
    private boolean enchanted;
    private int customModelData;
    private int amount;
}