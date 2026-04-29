package me.jetby.libb.gui.parser;

import me.jetby.libb.color.Serializer;

public record ParserRule(Serializer serializer) {

    public static ParserRule of(Serializer serializer) {
        return new ParserRule(serializer);
    }
}
