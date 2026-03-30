package me.jetby.libb.action.record;

import java.util.List;

public record Expression(
        String input,
        List<String> success,
        List<String> fail
) {
}
