package net.jeqo.gizmo;

import lombok.Getter;
import net.jeqo.gizmo.data.*;
import net.jeqo.gizmo.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public final class Gizmo extends JavaPlugin {

    public static final HashMap<UUID, String> playerTracker = new HashMap<>();
    @Getter
    public static Gizmo instance;

    @Override
    public void onEnable() {
        instance = this;
        loadListeners();
        Objects.requireNonNull(getCommand("gizmo")).setExecutor(new Commands(this));

        getConfig().options().copyDefaults();
        saveDefaultConfig();
        createScreensConfig();
        createMessagesConfig();
        getLogger().info("Gizmo successfully loaded! Made by Jeqo");
    }

    public void loadListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerScreening(), this);
        Bukkit.getPluginManager().registerEvents(new ScreenHandlers(), this);
        Bukkit.getPluginManager().registerEvents(new ScreenAdvance(), this);
        Bukkit.getPluginManager().registerEvents(new ClickableItems(), this);
    }

    @Getter
    private FileConfiguration screensConfig;
    private void createScreensConfig() {
        // screens.yml
        File screensConfigFile = new File(getDataFolder(), "screens.yml");
        if (!screensConfigFile.exists()) {
            if (screensConfigFile.getParentFile().mkdirs()) {
                saveResource("screens.yml", false);
            }
        }

        screensConfig = new YamlConfiguration();
        try {
            screensConfig.load(screensConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().warning("Error while loading screens config! " + e.getMessage());
        }
    }


    @Getter
    private FileConfiguration messagesConfig;
    private void createMessagesConfig() {
        // messages.yml
        File messagesConfigFile = new File(getDataFolder(), "messages.yml");
        if (!messagesConfigFile.exists()) {
            if(messagesConfigFile.getParentFile().mkdirs()) {
                saveResource("messages.yml", false);
            }
        }

        messagesConfig = new YamlConfiguration();
        try {
            messagesConfig.load(messagesConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().warning("Error while loading messages! " + e.getMessage());
        }
    }
}
