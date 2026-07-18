package org.jetby.libb.test;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetby.libb.gui.parser.Gui;
import org.jetby.libb.gui.parser.Item;
import org.jetby.libb.gui.parser.ParsedGui;

import java.util.ArrayList;
import java.util.List;

public class ParsedGuiTest extends ParsedGui {

    public ParsedGuiTest(@NotNull Player viewer, @NotNull Gui guiDefinition, JavaPlugin plugin) {
        super(viewer, guiDefinition, plugin);

        List<Item> itemList = getBySectionOption("");
        itemList.forEach(item -> {
            setReplace(item, "{amount}", String.valueOf(item.amount()));
        });
    }

    @Override
    public void buildItems(List<Item> items) {
        for (Item item : items) {
            setReplace(item, "{amount}", String.valueOf(item.amount()));
        }
        super.buildItems(items);
    }
}
