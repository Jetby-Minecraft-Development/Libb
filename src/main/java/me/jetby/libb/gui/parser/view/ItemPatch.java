package me.jetby.libb.gui.parser.view;

import me.jetby.libb.action.record.ActionBlock;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;

import java.util.List;
import java.util.Map;

/**
 * A sealed hierarchy that represents every possible mutation that
 * view_requirements can apply to an item at display-time.
 *
 * Usage in config:
 * <pre>
 * view_requirements:
 *   vip_check:
 *     if: "%player_has_permission_myplugin.vip% == true"
 *     then:
 *       - display_name: "<gold>VIP Item"
 *       - lore:
 *           - "Line 1"
 *           - "Line 2"
 *       - lore[1]: "<red>Replaced line 2"
 *       - slot: 15
 *       - slots:
 *           - '10-14'
 *       - material: GOLD_INGOT
 *       - amount: 5
 *       - enchanted: true
 *       - custom_model_data: 1001
 *       - flags:
 *           - HIDE_ATTRIBUTES
 *       - on_click:
 *           any:
 *             - '[message] clicked'
 * </pre>
 */
public sealed interface ItemPatch permits
        ItemPatch.SetDisplayName,
        ItemPatch.SetLore,
        ItemPatch.SetLoreLine,
        ItemPatch.SetSlot,
        ItemPatch.SetSlots,
        ItemPatch.SetMaterial,
        ItemPatch.SetAmount,
        ItemPatch.SetEnchanted,
        ItemPatch.SetCustomModelData,
        ItemPatch.SetFlags,
        ItemPatch.SetOnClick {

    /** display_name: "<gold>Name" */
    record SetDisplayName(String miniMessageText) implements ItemPatch {}

    /** lore: ["line1", "line2"] — replaces entire lore */
    record SetLore(List<String> lines) implements ItemPatch {}

    /** lore[N]: "text" — replaces a single lore line (0-indexed) */
    record SetLoreLine(int index, String miniMessageText) implements ItemPatch {}

    /** slot: 12 — move item to a single slot */
    record SetSlot(int slot) implements ItemPatch {}

    /** slots: ['10-14', 20] — move item to multiple slots */
    record SetSlots(List<Integer> slots) implements ItemPatch {}

    /** material: DIAMOND */
    record SetMaterial(Material material) implements ItemPatch {}

    /** amount: 3 */
    record SetAmount(int amount) implements ItemPatch {}

    /** enchanted: true */
    record SetEnchanted(boolean enchanted) implements ItemPatch {}

    /** custom_model_data: 1001 */
    record SetCustomModelData(int value) implements ItemPatch {}

    /** flags: [HIDE_ATTRIBUTES, HIDE_ENCHANTS] */
    record SetFlags(List<ItemFlag> flags) implements ItemPatch {}

    /** on_click: { any: [...], left: [...], ... } */
    record SetOnClick(Map<ClickType, ActionBlock> clicks) implements ItemPatch {}
}