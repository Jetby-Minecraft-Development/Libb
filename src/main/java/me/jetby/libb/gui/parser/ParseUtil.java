package me.jetby.libb.gui.parser;

import me.jetby.libb.action.record.ActionBlock;
import me.jetby.libb.action.record.Expression;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseUtil {
    public static Map<ClickType, ActionBlock> getClicks(@NotNull ConfigurationSection section) {
        Map<ClickType, ActionBlock> clicks = new HashMap<>();

        ConfigurationSection onClickSec = section.getConfigurationSection("on_click");
        if (onClickSec == null) return clicks;

        for (String key : onClickSec.getKeys(false)) {
            switch (key) {
                case "any" -> clicks.put(null, ParseUtil.getActionBlock(onClickSec, key));
                case "left" -> clicks.put(ClickType.LEFT, ParseUtil.getActionBlock(onClickSec, key));
                case "shift_left" -> clicks.put(ClickType.SHIFT_LEFT, ParseUtil.getActionBlock(onClickSec, key));
                case "right" -> clicks.put(ClickType.RIGHT, ParseUtil.getActionBlock(onClickSec, key));
                case "shift_right" -> clicks.put(ClickType.SHIFT_RIGHT, ParseUtil.getActionBlock(onClickSec, key));
                case "middle" -> clicks.put(ClickType.MIDDLE, ParseUtil.getActionBlock(onClickSec, key));
                case "drop" -> clicks.put(ClickType.DROP, ParseUtil.getActionBlock(onClickSec, key));
                case "control_drop" -> clicks.put(ClickType.CONTROL_DROP, ParseUtil.getActionBlock(onClickSec, key));
                case "window_border_left" ->
                        clicks.put(ClickType.WINDOW_BORDER_LEFT, ParseUtil.getActionBlock(onClickSec, key));
                case "window_border_right" ->
                        clicks.put(ClickType.WINDOW_BORDER_RIGHT, ParseUtil.getActionBlock(onClickSec, key));
                case "double" -> clicks.put(ClickType.DOUBLE_CLICK, ParseUtil.getActionBlock(onClickSec, key));
                case "num_1", "num_2", "num_3", "num_4", "num_5", "num_6", "num_7", "num_8", "num_9" ->
                        clicks.put(ClickType.NUMBER_KEY, ParseUtil.getActionBlock(onClickSec, key));
            }
        }
        return clicks;

    }

    private static List<Integer> parseSlots(Object slotObject) {
        List<Integer> slots = new ArrayList<>();

        if (slotObject instanceof Integer) {
            slots.add((Integer) slotObject);
        } else if (slotObject instanceof String) {
            String slotString = ((String) slotObject).trim();
            slots.addAll(parseSlotString(slotString));
        } else if (slotObject instanceof List<?>) {
            for (Object obj : (List<?>) slotObject) {
                if (obj instanceof Integer) {
                    slots.add((Integer) obj);
                } else if (obj instanceof String) {
                    slots.addAll(parseSlotString((String) obj));
                }
            }
        } else {
            Bukkit.getLogger().warning("Unknown slot format: " + slotObject);
        }

        return slots;
    }

    private static List<Integer> parseSlotString(String slotString) {
        List<Integer> slots = new ArrayList<>();
        if (slotString.contains("-")) {
            try {
                String[] range = slotString.split("-");
                int start = Integer.parseInt(range[0].trim());
                int end = Integer.parseInt(range[1].trim());
                for (int i = start; i <= end; i++) {
                    slots.add(i);
                }
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning("Error parsing slot range: " + slotString);
            }
        } else {
            try {
                slots.add(Integer.parseInt(slotString));
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning("Error parsing single slot: " + slotString);
            }
        }
        return slots;
    }

    public static @Nullable List<Item> getItems(@NotNull FileConfiguration configuration) {
        ConfigurationSection items = configuration.getConfigurationSection("Items");
        if (items == null) return null;

        List<Item> itemList = new ArrayList<>();
        for (String key : items.getKeys(false)) {
            ConfigurationSection item = items.getConfigurationSection(key);
            if (item == null) continue;

            String type = item.getString("type");
            String displayName = item.getString("display_name");
            List<String> lore = item.getStringList("lore");

            String material = item.getString("material", "STONE").toUpperCase();
            ItemStack itemStack;
            if (material.startsWith("BASEHEAD-")) {
                try {
                    itemStack = SkullCreator.itemFromBase64(material.replace("BASEHEAD-", ""));
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error creating custom skull: " + e.getMessage());
                    itemStack = new ItemStack(SkullCreator.createSkull());
                }
            } else {
                itemStack = new ItemStack(Material.valueOf(material));
            }

            List<Integer> slots = new ArrayList<>();
            if (item.getInt("slot") <= 0) {
                slots.addAll(parseSlots(item.getStringList("slots")));
            } else {
                slots.add(item.getInt("slot"));
            }

            List<ItemFlag> flags = new ArrayList<>();
            for (String flag : item.getStringList("flags")) {
                flags.add(ItemFlag.valueOf(flag.toUpperCase()));
            }

            List<Enchantment> enchantments = new ArrayList<>();
            for (String enchantmentName : item.getStringList("enchantments")) {
                NamespacedKey k = NamespacedKey.minecraft(enchantmentName.toLowerCase());
                Enchantment enchantment = Registry.ENCHANTMENT.get(k);
                if (enchantment != null) {
                    enchantments.add(enchantment);
                }
            }

            Item finalItem = new Item(itemStack, type, displayName, lore, itemStack.getType(), slots, flags, enchantments);
            finalItem.onClick().putAll(getClicks(item));
            finalItem.section(item);
            itemList.add(finalItem);
        }

        return itemList;
    }

    public static @Nullable ActionBlock getActionBlock(@NotNull FileConfiguration configuration, @NotNull String path) {
        List<String> staticActions = new ArrayList<>();
        List<Expression> expressions = new ArrayList<>();

        List<?> list = configuration.getList(path);
        if (list == null) {
            return null;
        }

        for (Object object : list) {

            if (object instanceof String string) {
                staticActions.add(string);
                continue;
            }

            // - example_check: { ... }
            if (object instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {

                    String key = String.valueOf(entry.getKey());

                    if (!(entry.getValue() instanceof Map<?, ?> sectionMap)) {
                        continue;
                    }

                    ConfigurationSection section =
                            new MemoryConfiguration().createSection(key, sectionMap);

                    String expression = section.getString("if");
                    if (expression == null) {
                        continue;
                    }

                    List<String> success = section.getStringList("then");
                    List<String> fail = section.getStringList("else");

                    expressions.add(new Expression(expression, success, fail));
                }
            }
        }

        return new ActionBlock(staticActions, expressions);
    }

    public static @Nullable ActionBlock getActionBlock(@NotNull ConfigurationSection configuration, @NotNull String path) {
        List<String> staticActions = new ArrayList<>();
        List<Expression> expressions = new ArrayList<>();

        List<?> list = configuration.getList(path);
        if (list == null) {
            return null;
        }

        for (Object object : list) {

            if (object instanceof String string) {
                staticActions.add(string);
                continue;
            }

            // - example_check: { ... }
            if (object instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {

                    String key = String.valueOf(entry.getKey());

                    if (!(entry.getValue() instanceof Map<?, ?> sectionMap)) {
                        continue;
                    }

                    ConfigurationSection section =
                            new MemoryConfiguration().createSection(key, sectionMap);

                    String expression = section.getString("if");
                    if (expression == null) {
                        continue;
                    }

                    List<String> success = section.getStringList("then");
                    List<String> fail = section.getStringList("else");

                    expressions.add(new Expression(expression, success, fail));
                }
            }
        }

        return new ActionBlock(staticActions, expressions);
    }

}
