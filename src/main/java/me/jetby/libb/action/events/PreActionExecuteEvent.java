package me.jetby.libb.action.events;

import lombok.Getter;
import me.jetby.libb.action.ActionContext;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class PreActionExecuteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final ActionContext ctx;
    private final String command;

    public PreActionExecuteEvent(ActionContext ctx, String command) {
        this.ctx = ctx;
        this.command = command;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
