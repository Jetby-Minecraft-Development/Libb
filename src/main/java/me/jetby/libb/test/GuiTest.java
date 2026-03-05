package me.jetby.libb.test;

import me.jetby.libb.gui.AdvancedGui;
import me.jetby.libb.gui.item.ItemWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public class GuiTest extends AdvancedGui {


    public GuiTest(String title) {
        super(title);


        ItemWrapper item = new ItemWrapper(Material.STONE);
        item.slots(1);
        item.setDisplayName("");

        setItem("example", ItemWrapper.builder(Material.STONE)
                .slots(1, 5, 7)
                .displayName(Component.text("This is the name dude"))
                .onClick(event -> {
                    event.setCancelled(true);
                    player.sendMessage("Clicked on slot: "+event.getSlot());
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
