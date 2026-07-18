package org.jetby.libb.platform;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetby.libb.AdventureReflect;
import org.jetby.libb.LibbApi;

import java.util.List;
import java.util.stream.Collectors;

public class PlatformMeta {

    private static String serialize(Component component) {
        return AdventureReflect.toLegacySection(component);
    }

    private static Component deserialize(String text) {
        return AdventureReflect.legacySection(text);
    }

    public static void setDisplayName(ItemMeta meta, Component component) {
        if (component==null) return;
        if (LibbApi.Settings.PLATFORM == Platform.PAPER) {
            meta.displayName(component);
        } else {
            meta.setDisplayName(serialize(component));
        }
    }

    public static Component getDisplayName(ItemMeta meta) {
        if (LibbApi.Settings.PLATFORM == Platform.PAPER) {
            return meta.displayName();
        } else {
            String name = meta.getDisplayName();
            return name == null || name.isEmpty() ? null : deserialize(name);
        }
    }

    public static void setLore(ItemMeta meta, List<Component> lore) {
        if (lore==null || lore.isEmpty()) return;
        if (LibbApi.Settings.PLATFORM == Platform.PAPER) {
            meta.lore(lore);
        } else {
            meta.setLore(lore.stream().map(PlatformMeta::serialize).collect(Collectors.toList()));
        }
    }

    public static List<Component> getLore(ItemMeta meta) {
        if (LibbApi.Settings.PLATFORM == Platform.PAPER) {
            return meta.lore();
        } else {
            List<String> lore = meta.getLore();
            if (lore == null) return null;
            return lore.stream().map(PlatformMeta::deserialize).collect(Collectors.toList());
        }
    }
}