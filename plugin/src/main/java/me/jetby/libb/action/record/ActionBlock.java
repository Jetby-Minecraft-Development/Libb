package me.jetby.libb.action.record;

import java.util.List;

public record ActionBlock(
        List<String> staticActions,
        List<Expression> expressions
) {
}