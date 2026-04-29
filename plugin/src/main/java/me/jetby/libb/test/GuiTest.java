package me.jetby.libb.test;

import me.jetby.libb.color.Serializer;
import me.jetby.libb.gui.AdvancedGui;
import me.jetby.libb.gui.item.ItemWrapper;
import org.bukkit.Material;

public class GuiTest extends AdvancedGui {
    public GuiTest(String title) {
        super(title);

        defaultSerializer = Serializer.UNIFIED;


        setItem("example", ItemWrapper.builder(Material.STONE)
                .slots(1, 5, 7)
                .setDisplayName("&cThis is the name dude")
                .onClick(event -> {
                    event.setCancelled(true);
                    player.sendMessage("Clicked on slot: <yellow>" + event.getSlot());
                })
                .build());

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
