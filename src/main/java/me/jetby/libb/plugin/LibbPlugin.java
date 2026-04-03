package me.jetby.libb.plugin;

import lombok.Getter;
import me.jetby.libb.Libb;
import me.jetby.libb.command.CommandRegistrar;
import me.jetby.libb.util.Metrics;
import me.jetby.libb.util.VersionUtil;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

// This class is still in process
public class LibbPlugin extends JavaPlugin {

    public boolean isDebug;

    @Getter
    private final String version;
    @Getter
    private VersionUtil versionUtil;

    public LibbPlugin() {
        this.version = getDescription().getVersion();
        Libb.HOOKED_PLUGINS.add(this);
    }

    public boolean debug(boolean debug) {
        this.isDebug = debug;
        return debug;
    }

    public void setVersionUtil(VersionUtil versionUtil) {
        this.versionUtil = versionUtil;
    }

    public void setVersionUtil(@NotNull String updateLink, @NotNull String permission) {
        versionUtil = new VersionUtil(this, version, updateLink, permission);
    }

    public void setVersionUtil(@NotNull String updateLink) {
        versionUtil = new VersionUtil(this, version, updateLink, getName().toLowerCase() + ".update");
    }

    /**
     * Creates a new Metrics instance.
     *
     * @param plugin    Your plugin instance.
     * @param serviceId The id of the service. It can be found at <a
     *                  href="https://bstats.org/what-is-my-plugin-id">What is my plugin id?</a>
     */
    public void setBStats(Plugin plugin, int serviceId) {
        new Metrics(plugin, serviceId);
    }

    public void registerCommand(JavaPlugin plugin, String commandName, @NotNull CommandExecutor executor) {
        CommandRegistrar.registerCommand(plugin, commandName, executor);
    }

    public void unregisterCommands() {
        CommandRegistrar.unregisterAll(this);
    }

    public FileConfiguration getFileConfiguration(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) saveResource(fileName, false);
        return YamlConfiguration.loadConfiguration(file);
    }

    public File getFile(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) saveResource(fileName, false);
        return file;
    }
}
