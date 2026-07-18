package org.jetby.libb.gui.parser;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetby.libb.LibbApi;
import org.jetby.libb.action.ActionUtil;
import org.jetby.libb.action.record.ActionBlock;
import org.jetby.libb.action.record.Expression;
import org.jetby.libb.platform.Platform;

import java.util.*;

public class ParseUtil {
    public static Map<ClickType, ActionBlock> getClicks(@NotNull ConfigurationSection section) {
        Map<ClickType, ActionBlock> clicks = new HashMap<>();

        ConfigurationSection onClickSec = section.getConfigurationSection("on_click");
        if (onClickSec == null) return clicks;

        for (String key : onClickSec.getKeys(false)) {
            switch (key) {
                case "any" -> clicks.put(null, ActionUtil.getActionBlock(onClickSec, key));
                case "left" -> clicks.put(ClickType.LEFT, ActionUtil.getActionBlock(onClickSec, key));
                case "shift_left" -> clicks.put(ClickType.SHIFT_LEFT, ActionUtil.getActionBlock(onClickSec, key));
                case "right" -> clicks.put(ClickType.RIGHT, ActionUtil.getActionBlock(onClickSec, key));
                case "shift_right" -> clicks.put(ClickType.SHIFT_RIGHT, ActionUtil.getActionBlock(onClickSec, key));
                case "middle" -> clicks.put(ClickType.MIDDLE, ActionUtil.getActionBlock(onClickSec, key));
                case "drop" -> clicks.put(ClickType.DROP, ActionUtil.getActionBlock(onClickSec, key));
                case "control_drop" -> clicks.put(ClickType.CONTROL_DROP, ActionUtil.getActionBlock(onClickSec, key));
                case "window_border_left" ->
                        clicks.put(ClickType.WINDOW_BORDER_LEFT, ActionUtil.getActionBlock(onClickSec, key));
                case "window_border_right" ->
                        clicks.put(ClickType.WINDOW_BORDER_RIGHT, ActionUtil.getActionBlock(onClickSec, key));
                case "double" -> clicks.put(ClickType.DOUBLE_CLICK, ActionUtil.getActionBlock(onClickSec, key));
                case "num_1", "num_2", "num_3", "num_4", "num_5", "num_6", "num_7", "num_8", "num_9" ->
                        clicks.put(ClickType.NUMBER_KEY, ActionUtil.getActionBlock(onClickSec, key));
            }
        }
        return clicks;

    }

    public static List<Integer> parseSlots(Object slotObject) {
        List<Integer> slots = new ArrayList<>();

        switch (slotObject) {
            case Number number -> slots.add(number.intValue());

            case String string -> slots.addAll(parseSlotString(string.trim()));

            case List<?> objects -> {
                for (Object obj : objects) {
                    slots.addAll(parseSlots(obj));
                }
            }

            case null, default ->
                    throw new RuntimeException("Unknown slot format: " + slotObject);
        }

        return slots;
    }

    private static List<Integer> parseSlotString(String slotString) {
        List<Integer> slots = new ArrayList<>();

        // "15,16,17"
        if (slotString.contains(",")) {
            String[] split = slotString.split(",");

            for (String part : split) {
                slots.addAll(parseSlotString(part.trim()));
            }

            return slots;
        }

        // "15-20"
        if (slotString.contains("-")) {
            try {
                String[] range = slotString.split("-");

                int start = Integer.parseInt(range[0].trim());
                int end = Integer.parseInt(range[1].trim());

                for (int i = start; i <= end; i++) {
                    slots.add(i);
                }

            } catch (NumberFormatException e) {
                throw new RuntimeException("Error parsing slot range: " + slotString);
            }

            return slots;
        }

        // "15"
        try {
            slots.add(Integer.parseInt(slotString));

        } catch (NumberFormatException e) {
            throw new RuntimeException("Error parsing single slot: " + slotString);
        }

        return slots;
    }

    public static @Nullable List<Item> getItems(@NotNull FileConfiguration configuration) {
        ConfigurationSection items = configuration.getConfigurationSection("Items");
        if (items == null) return null;

        List<Item> itemList = new ArrayList<>();
        for (String key : items.getKeys(false)) {
            ConfigurationSection section = items.getConfigurationSection(key);
            if (section == null) continue;

            String type = section.getString("type");
            String displayName = section.getString("display_name");
            List<String> lore = section.getStringList("lore");
            Object customModelData = section.get("custom-model-data");
            int amount = section.getInt("amount", 1);

            String material = section.getString("material", "STONE");
            ItemStack itemStack;

            if (material.regionMatches(true, 0, "BASEHEAD-", 0, "BASEHEAD-".length())) {
                try {
                    String base64 = material.substring("BASEHEAD-".length());
                    itemStack = SkullCreator.itemFromBase64(base64);
                } catch (Exception e) {
                    itemStack = new ItemStack(SkullCreator.createSkull());
                    throw new RuntimeException("Error creating custom skull:", e);
                }
            } else {
                itemStack = new ItemStack(Material.valueOf(material.toUpperCase()));
            }
            if (LibbApi.Settings.PLATFORM == Platform.PAPER) {
                itemStack.setData(DataComponentTypes.MAX_STACK_SIZE, 99);

            }
            itemStack.setAmount(amount);

            List<Integer> slots = parseSlots(section.get("slot")==null ? section.get("slots") : section.get("slot"));

            List<ItemFlag> flags = new ArrayList<>();
            for (String flagName : section.getStringList("flags")) {
                try {
                    ItemFlag itemFlag = ItemFlag.valueOf(flagName.toUpperCase());
                    flags.add(itemFlag);
                } catch (IllegalArgumentException ignored) {
                }
            }

            List<Enchantment> enchantments = new ArrayList<>();
            for (String enchantmentName : section.getStringList("enchantments")) {
                NamespacedKey k = NamespacedKey.minecraft(enchantmentName.toLowerCase());
                Enchantment enchantment = Registry.ENCHANTMENT.get(k);
                if (enchantment != null) {
                    enchantments.add(enchantment);
                }
            }

            Item item = new Item(itemStack);
            item.amount(amount);
            item.customModelData(customModelData);
            item.type(type);
            item.displayName(displayName);
            item.lore(lore);
            item.material(itemStack.getType());
            item.slots(slots);
            item.flags(flags);
            item.enchantments(enchantments);
            item.onClick().putAll(getClicks(section));
            item.section(section);
            item.viewRequirements(section.getStringList("view_requirements"));
            item.enchanted(section.getBoolean("enchanted", false));

            if (section.contains("priority")) {
                item.priority(section.getInt("priority"));
            }

            itemList.add(item);
        }

        return itemList;
    }

    /** @deprecated in favour of {@link ActionUtil#getActionBlock(List)}*/
    @Deprecated(forRemoval = true, since = "1.2.3")
    public static ActionBlock getActionBlock(List<?> list) {
        return ActionUtil.getActionBlock(list);
    }

    /** @deprecated in favour of {@link ActionUtil#getExpressions(List)}*/
    @Deprecated(forRemoval = true, since = "1.2.3")
    public static @NotNull List<Expression> getExpressions(@NotNull List<?> list) {
        return ActionUtil.getExpressions(list);
    }

    /** @deprecated in favour of {@link ActionUtil#parseExpression(Object)}*/
    @Deprecated(forRemoval = true, since = "1.2.3")
    public static @NotNull Optional<Expression> parseExpression(@Nullable Object object) {
       return ActionUtil.parseExpression(object);
    }

    /** @deprecated in favour of {@link ActionUtil#getActionBlock(org.bukkit.configuration.Configuration configuration, String path)}*/
    @Deprecated(forRemoval = true, since = "1.2.3")
    public static ActionBlock getActionBlock(@NotNull FileConfiguration configuration, String path) {
        return ActionUtil.getActionBlock(configuration.getList(path));
    }
    /** @deprecated in favour of {@link ActionUtil#getActionBlock(ConfigurationSection configuration, String path)}*/
    @Deprecated(forRemoval = true, since = "1.2.3")
    public static ActionBlock getActionBlock(ConfigurationSection configuration, String path) {
        return ActionUtil.getActionBlock(configuration.getList(path));
    }

}