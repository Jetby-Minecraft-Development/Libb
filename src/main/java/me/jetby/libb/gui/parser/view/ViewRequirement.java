package me.jetby.libb.gui.parser.view;

import java.util.List;

/**
 * Represents a single view_requirements entry.
 * If the expression evaluates to true, all patches are applied to the item.
 */
public record ViewRequirement(
        String expression,          // e.g. "%num% > 6"
        List<ItemPatch> patches      // modifications to apply when expression is true
) {}