package me.jetby.libb;

import me.jetby.libb.configuration.GuisConfiguration;
import me.jetby.libb.gui.AdvancedGui;
import me.jetby.libb.gui.GuiListener;
import me.jetby.libb.gui.parser.Gui;
import me.jetby.libb.plugin.LibbPlugin;
import me.jetby.libb.util.Logger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public final class Libb extends LibbPlugin {


    public static final Map<String, Gui> PARSED_GUIS = new HashMap<>();

    public static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static final Set<Plugin> HOOKED_PLUGINS = new HashSet<>();

    public GuisConfiguration guisConfiguration;

    public static Libb INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;

        Logger.info(this, "<#1CFB00>╔");
        Logger.info(this, "<#1CFB00>║  <#0AD7FB>Libb <red>v" + getVersion());
        Logger.info(this, "<#1CFB00>║");
        setBStats(this, 30288);
        setVersionUtil("https://raw.githubusercontent.com/MrJetby/Libb/refs/heads/master/VERSION");
        new LibbCommand(this).register();
        guisConfiguration = new GuisConfiguration(this);
        guisConfiguration.load();
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
        Logger.info(this, "<#1CFB00>║  <#0AD7FB>" + Libb.PARSED_GUIS.size() + " guis loaded");
        Logger.info(this, "<#1CFB00>║");
        Logger.info(this, "<#1CFB00>║  <#1CFB00>Plugin enabled");
        Logger.info(this, "<#1CFB00>╚");

    }

    @Override
    public void onDisable() {
        Logger.info(this, "<#FB0000>╔");
        Logger.info(this, "<#FB0000>║  <#0AD7FB>Libb <red>v" + getVersion());

        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory topInventory = player.getOpenInventory().getTopInventory();
            if (!(topInventory instanceof AdvancedGui)) continue;
            System.out.println("founded gui");
            topInventory.close();
        }
        Logger.info(this, "<#FB0000>║  <#FB0000>Plugin disabled");
        Logger.info(this, "<#FB0000>╚");
    }
}
