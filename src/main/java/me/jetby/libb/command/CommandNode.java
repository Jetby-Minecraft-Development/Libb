package me.jetby.libb.command;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandNode {
    @Setter
    @Getter
    private MethodSubCommand executor;
    private final Map<String, CommandNode> children = new HashMap<>();

    public CommandNode getOrCreate(String key) {
        return children.computeIfAbsent(key.toLowerCase(), k -> new CommandNode());
    }

    public CommandNode get(String key) {
        return children.get(key.toLowerCase());
    }

    public Set<String> childrenKeys() {
        return children.keySet();
    }

}