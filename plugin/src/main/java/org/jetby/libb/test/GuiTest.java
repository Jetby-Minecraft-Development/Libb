package org.jetby.libb.test;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetby.libb.color.Serializer;
import org.jetby.libb.gui.AdvancedGui;
import org.jetby.libb.gui.item.ItemWrapper;

public class GuiTest extends AdvancedGui {
    public GuiTest(String title) {
        super(title);

        defaultSerializer = Serializer.UNIFIED;

        lockEmptySlots(true);

        setItem("magic_diamond", ItemWrapper.builder(Material.DIAMOND)
                .slots(13)
                .setDisplayName("&cMagic diamond")
                .onClick(event -> {
                    Player player = (Player) event.getWhoClicked();
                    if (!player.getInventory().contains(Material.GOLD_INGOT, 10)) {
                        player.sendMessage(defaultSerializer.deserialize("&c&lYou need at least 10 golden ingots to shop"));
                        return;
                    }
                    player.getInventory().remove(new ItemStack(Material.GOLD_INGOT, 10));
                    ItemStack item = new ItemStack(Material.DIAMOND);
                    ItemMeta meta = item.getItemMeta();
                    meta.displayName(defaultSerializer.deserialize("&b&lMagic diamond"));
                    meta.addEnchant(Enchantment.KNOCKBACK, 1, false);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    item.setItemMeta(meta);
                    player.getInventory().addItem(item);

                }).build());


        onOpen(event -> {
            event.getPlayer().sendMessage("open");
        });
        onClose(event -> {
            event.getPlayer().sendMessage("close");
        });
        onDrag(event -> {
            event.getWhoClicked().sendMessage("drag");
        });

    }

}
