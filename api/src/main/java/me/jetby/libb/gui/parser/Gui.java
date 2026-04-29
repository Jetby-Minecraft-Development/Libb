package me.jetby.libb.gui.parser;



import me.jetby.libb.action.record.ActionBlock;

import java.util.List;

public record Gui(
        String id,
        String title,
        int size,
        List<String> command,
        List<String> preOpenExpressions,
        ActionBlock onOpen,
        ActionBlock onClose,
        List<Item> items
) {

}
