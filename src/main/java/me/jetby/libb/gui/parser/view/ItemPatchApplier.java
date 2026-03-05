package me.jetby.libb.gui.parser.view;


import me.clip.placeholderapi.PlaceholderAPI;
import me.jetby.libb.Libb;
import me.jetby.libb.action.ActionContext;
import me.jetby.libb.action.ActionExecute;
import me.jetby.libb.gui.AdvancedGui;
import me.jetby.libb.gui.item.ItemWrapper;
import me.jetby.libb.gui.parser.ParsedGui;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies a list of {@link ItemPatch} objects to an {@link ItemWrapper}.
 * Java 17 compatible — uses instanceof checks instead of switch pattern matching.
 * <p>
 * Call {@link #apply(ItemWrapper, List, Player, AdvancedGui)} after evaluating
 * which {@link ViewRequirement}s are satisfied.
 */
public final class ItemPatchApplier {

    private ItemPatchApplier() {
    }

    /**
     * Applies every patch in {@code patches} to {@code wrapper}.
     * PlaceholderAPI replacements are done for the given {@code player}.
     * The inventory is updated via {@code gui}.
     */
    public static void apply(ItemWrapper wrapper,
                             List<ItemPatch> patches,
                             Player player,
                             AdvancedGui gui) {

        ItemStack itemStack = wrapper.itemStack();
        if (itemStack == null) return;

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;

        List<Component> workingLore = meta.lore() != null
                ? new ArrayList<>(meta.lore())
                : new ArrayList<>();

        boolean metaDirty = false;
        boolean slotsDirty = false;

        for (ItemPatch patch : patches) {

            if (patch instanceof ItemPatch.SetDisplayName p) {
                String resolved = PlaceholderAPI.setPlaceholders(player, p.miniMessageText());
                Component name = Libb.MINI_MESSAGE.deserialize("<italic:false>" + resolved);
                meta.displayName(name);
                wrapper.displayName(name);
                metaDirty = true;

            } else if (patch instanceof ItemPatch.SetLore p) {
                workingLore.clear();
                for (String line : p.lines()) {
                    String resolved = PlaceholderAPI.setPlaceholders(player, line);
                    workingLore.add(Libb.MINI_MESSAGE.deserialize("<italic:false>" + resolved));
                }
                metaDirty = true;

            } else if (patch instanceof ItemPatch.SetLoreLine p) {
                while (workingLore.size() <= p.index()) {
                    workingLore.add(Component.empty());
                }
                String resolved = PlaceholderAPI.setPlaceholders(player, p.miniMessageText());
                workingLore.set(p.index(),
                        Libb.MINI_MESSAGE.deserialize("<italic:false>" + resolved));
                metaDirty = true;

            } else if (patch instanceof ItemPatch.SetSlot p) {
                wrapper.slots(p.slot());
                slotsDirty = true;

            } else if (patch instanceof ItemPatch.SetSlots p) {
                wrapper.slots(p.slots().toArray(new Integer[0]));
                slotsDirty = true;

            } else if (patch instanceof ItemPatch.SetMaterial p) {
                ItemStack newStack = new ItemStack(p.material(), itemStack.getAmount());
                ItemMeta newMeta = newStack.getItemMeta();
                if (newMeta != null) {
                    newMeta.displayName(meta.displayName());
                    newMeta.lore(meta.lore());
                    for (org.bukkit.NamespacedKey key : meta.getPersistentDataContainer().getKeys()) {
                        newMeta.getPersistentDataContainer().set(
                                key,
                                org.bukkit.persistence.PersistentDataType.STRING,
                                meta.getPersistentDataContainer().getOrDefault(
                                        key, org.bukkit.persistence.PersistentDataType.STRING, ""));
                    }
                    newStack.setItemMeta(newMeta);
                }
                wrapper.itemStack(newStack);
                wrapper.material(p.material());
                itemStack = newStack;
                meta = newStack.getItemMeta();
                metaDirty = false;
                slotsDirty = true;

            } else if (patch instanceof ItemPatch.SetAmount p) {
                itemStack.setAmount(p.amount());
                wrapper.amount(p.amount());

            } else if (patch instanceof ItemPatch.SetEnchanted p) {
                wrapper.enchanted(p.enchanted());
                if (p.enchanted()) {
                    meta.addEnchant(Enchantment.OXYGEN, 1, false);
                } else {
                    meta.removeEnchant(Enchantment.OXYGEN);
                }
                metaDirty = true;

            } else if (patch instanceof ItemPatch.SetCustomModelData p) {
                meta.setCustomModelData(p.value());
                wrapper.customModelData(p.value());
                metaDirty = true;

            } else if (patch instanceof ItemPatch.SetFlags p) {
                for (ItemFlag flag : ItemFlag.values()) meta.removeItemFlags(flag);
                wrapper.flags(p.flags().toArray(new ItemFlag[0]));
                for (ItemFlag flag : p.flags()) meta.addItemFlags(flag);
                metaDirty = true;

            } else if (patch instanceof ItemPatch.SetOnClick p) {
                wrapper.onClick(event -> {
                    event.setCancelled(true);
                    Player clicker = (Player) event.getWhoClicked();

                    if (p.clicks().containsKey(null))
                        ActionExecute.run(ActionContext.of(clicker)
                                        .with(wrapper)
                                        .with(ParsedGui.class)
                                        .with(gui),
                                p.clicks().get(null));

                    p.clicks().forEach((clickType, block) -> {
                        if (clickType == null) return;
                        if (!event.getClick().equals(clickType)) return;
                        ActionExecute.run(ActionContext.of(clicker)
                                .with(wrapper)
                                .with(gui)
                                .with(ParsedGui.class), block);
                    });
                });
            }
        }

        if (metaDirty && meta != null) {
            meta.lore(workingLore);
            itemStack.setItemMeta(meta);
            wrapper.itemStack(itemStack);
        }

        if ((metaDirty || slotsDirty) && wrapper.slots() != null) {
            for (int slot : wrapper.slots()) {
                gui.getInventory().setItem(slot, wrapper.itemStack());
            }
        }
    }
}