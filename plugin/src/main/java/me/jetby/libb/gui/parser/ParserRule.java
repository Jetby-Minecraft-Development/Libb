package me.jetby.libb.gui.parser;

import me.jetby.libb.color.HashedSerializer;

public record ParserRule(HashedSerializer serializer) {

    public static ParserRule of(HashedSerializer serializer) {
        return new ParserRule(serializer);
    }
}
