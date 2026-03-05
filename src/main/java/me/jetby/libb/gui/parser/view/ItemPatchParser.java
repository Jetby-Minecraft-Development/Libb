package me.jetby.libb.gui.parser.view;

import me.jetby.libb.gui.parser.ParseUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parses the {@code view_requirements} block from a YAML item section into a list
 * of {@link ViewRequirement} objects.
 *
 * <h3>YAML format</h3>
 * <pre>
 * view_requirements:
 *   my_check:                        # arbitrary name, used only for logging
 *     if: "%player_health% < 5"      # PlaceholderAPI expression
 *     then:                          # list of patches applied when true
 *       - display_name: "<red>Dying!"
 *       - lore:
 *           - "You are almost dead"
 *       - lore[0]: "<red>First line replaced"
 *       - slot: 20
 *       - slots:
 *           - '10-14'
 *           - 20
 *       - material: REDSTONE
 *       - amount: 64
 *       - enchanted: true
 *       - custom_model_data: 42
 *       - flags:
 *           - HIDE_ATTRIBUTES
 *       - on_click:
 *           any:
 *             - '[message] you clicked'
 * </pre>
 */
public final class ItemPatchParser {

    private ItemPatchParser() {}

    /**
     * Reads the {@code view_requirements} section from an item's
     * {@link ConfigurationSection} and returns all parsed requirements.
     */
    public static List<ViewRequirement> parse(ConfigurationSection itemSection) {
        List<ViewRequirement> result = new ArrayList<>();

        ConfigurationSection reqSection = itemSection.getConfigurationSection("view_requirements");
        if (reqSection == null) return result;

        for (String checkName : reqSection.getKeys(false)) {
            ConfigurationSection check = reqSection.getConfigurationSection(checkName);
            if (check == null) continue;

            String expression = check.getString("if");
            if (expression == null) {
                Bukkit.getLogger().warning(
                        "[Libb] view_requirements." + checkName + " is missing 'if' field, skipping.");
                continue;
            }

            List<?> thenList = check.getList("then");
            List<ItemPatch> patches = parsePatches(thenList, checkName);

            result.add(new ViewRequirement(expression, patches));
        }

        return result;
    }

    // -----------------------------------------------------------------------
    // Internal
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static List<ItemPatch> parsePatches(List<?> rawList, String checkName) {
        List<ItemPatch> patches = new ArrayList<>();
        if (rawList == null) return patches;

        for (Object raw : rawList) {
            if (!(raw instanceof Map<?, ?> map)) continue;

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                Object value = entry.getValue();

                try {
                    ItemPatch patch = parsePatch(key, value);
                    if (patch != null) patches.add(patch);
                } catch (Exception e) {
                    Bukkit.getLogger().warning(
                            "[Libb] Failed to parse patch '" + key + "' in view_requirements." + checkName
                                    + ": " + e.getMessage());
                }
            }
        }

        return patches;
    }

    @SuppressWarnings("unchecked")
    private static ItemPatch parsePatch(String key, Object value) {

        // display_name: "<gold>Name"
        if (key.equals("display_name")) {
            return new ItemPatch.SetDisplayName(String.valueOf(value));
        }

        // lore: ["line1", "line2"]
        if (key.equals("lore") && value instanceof List<?> list) {
            List<String> lines = new ArrayList<>();
            for (Object o : list) lines.add(String.valueOf(o));
            return new ItemPatch.SetLore(lines);
        }

        // lore[N]: "text"
        if (key.startsWith("lore[") && key.endsWith("]")) {
            int index = Integer.parseInt(key.substring(5, key.length() - 1));
            return new ItemPatch.SetLoreLine(index, String.valueOf(value));
        }

        // slot: 12
        if (key.equals("slot")) {
            return new ItemPatch.SetSlot(toInt(value));
        }

        // slots: ['10-14', 20]
        if (key.equals("slots") && value instanceof List<?>) {
            List<Integer> slots = parseSlotList(value);
            return new ItemPatch.SetSlots(slots);
        }

        // material: DIAMOND
        if (key.equals("material")) {
            return new ItemPatch.SetMaterial(Material.valueOf(String.valueOf(value).toUpperCase()));
        }

        // amount: 3
        if (key.equals("amount")) {
            return new ItemPatch.SetAmount(toInt(value));
        }

        // enchanted: true
        if (key.equals("enchanted")) {
            return new ItemPatch.SetEnchanted(Boolean.parseBoolean(String.valueOf(value)));
        }

        // custom_model_data: 42
        if (key.equals("custom_model_data")) {
            return new ItemPatch.SetCustomModelData(toInt(value));
        }

        // flags: [HIDE_ATTRIBUTES]
        if (key.equals("flags") && value instanceof List<?> list) {
            List<ItemFlag> flags = new ArrayList<>();
            for (Object o : list) flags.add(ItemFlag.valueOf(String.valueOf(o).toUpperCase()));
            return new ItemPatch.SetFlags(flags);
        }

        // on_click: { any: [...], left: [...] }
        if (key.equals("on_click") && value instanceof Map<?, ?> rawMap) {
            // Re-use ParseUtil by building a temporary MemoryConfiguration
            org.bukkit.configuration.MemoryConfiguration tmp = new org.bukkit.configuration.MemoryConfiguration();
            tmp.createSection("on_click", (Map<?, ?>) rawMap);
            return new ItemPatch.SetOnClick(ParseUtil.getClicks(tmp.getConfigurationSection("")));
        }

        Bukkit.getLogger().warning("[Libb] Unknown view_requirements patch key: '" + key + "'");
        return null;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static int toInt(Object o) {
        if (o instanceof Number n) return n.intValue();
        return Integer.parseInt(String.valueOf(o).trim());
    }

    private static List<Integer> parseSlotList(Object slotObject) {
        List<Integer> slots = new ArrayList<>();
        if (!(slotObject instanceof List<?> list)) return slots;

        for (Object obj : list) {
            if (obj instanceof Integer i) {
                slots.add(i);
            } else if (obj instanceof String s) {
                s = s.trim();
                if (s.contains("-")) {
                    String[] parts = s.split("-");
                    int start = Integer.parseInt(parts[0].trim());
                    int end = Integer.parseInt(parts[1].trim());
                    for (int i = start; i <= end; i++) slots.add(i);
                } else {
                    slots.add(Integer.parseInt(s));
                }
            }
        }
        return slots;
    }
}