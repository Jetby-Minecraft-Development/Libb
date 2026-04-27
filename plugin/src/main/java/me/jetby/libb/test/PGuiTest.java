package me.jetby.libb.test;

import me.jetby.libb.color.Serializer;
import me.jetby.libb.gui.item.ItemWrapper;
import me.jetby.libb.gui.parser.Gui;
import me.jetby.libb.gui.parser.ParsedGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PGuiTest extends ParsedGui {

    public PGuiTest(Player player, JavaPlugin plugin, Gui gui) {
        super(player, gui, plugin);
        defaultSerializer = Serializer.UNIFIED;
        lockEmptySlots(true);

        contentSlots(10, 11, 12, 13, 14, 15);

        for (int i = 0; i < 100; i++) {
            addItem(ItemWrapper.builder(Material.DIAMOND)
                    .setDisplayName("&cnum: &e" + i)
                    .build());
        }
    }

    @Override
    public void everyPageLogic() {
        setItem(NEXT_KEY, ItemWrapper.builder(Material.ARROW)
                .slots(24)
                .onClick(event -> {
                    event.setCancelled(true);
                    nextPage();
                }).build());

        setItem(PREV_KEY, ItemWrapper.builder(Material.ARROW)
                .slots(22)
                .onClick(event -> {
                    event.setCancelled(true);
                    prevPage();
                }).build());
    }
}