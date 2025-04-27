package net.jeqo.gizmo.listeners;

import net.jeqo.gizmo.Gizmo;
import net.jeqo.gizmo.data.Utilities;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.jeqo.gizmo.data.Placeholders.*;
import static net.jeqo.gizmo.data.Utilities.*;

public class PlayerScreening implements Listener {

    static final Gizmo plugin = Gizmo.getInstance();
    public static final HashMap<UUID, ItemStack[]> saveInv = new HashMap<>();
    public static final HashMap<UUID, Boolean> playersScreenActive = new HashMap<>();

    // Resource pack status event
    @EventHandler
    public void onPackAccept(PlayerResourcePackStatusEvent e) {
        Player p = e.getPlayer();
        switch (e.getStatus()) {
            case ACCEPTED:
                // Display the background Unicode during the delay
                if (Objects.equals(plugin.getConfig().getBoolean("delay-background"), true)) {
                    Utilities.showTitle(p, createComponent("background-color" + pullScreensConfig("Unicodes.background")), null, 0, 999999, 0);
                }
                break;
            case SUCCESSFULLY_LOADED:
                // Play a configured sound when the pack is loaded
                if (Objects.equals(plugin.getConfig().getBoolean("sound-on-pack-load.enable"), true)) {
                    String soundName = plugin.getConfig().getString("sound-on-pack-load.sound");
                    Registry<@NotNull Sound> soundRegistry = Registry.SOUNDS;
                    Sound sound = soundName != null ? soundRegistry.get(NamespacedKey.minecraft(soundName.toLowerCase())) : null;

                    if (soundName != null && sound != null) {
                        float volume = Float.parseFloat(plugin.getConfig().getString("sound-on-pack-load.volume", "0.0"));
                        float pitch = Float.parseFloat(plugin.getConfig().getString("sound-on-pack-load.pitch", "0.0"));

                        if (volume > 0.0 && pitch > 0.0) {
                            p.playSound(p.getLocation(), sound, volume, pitch);
                        }
                    }
                }
                // Display first time welcome screen
                if (!p.hasPlayedBefore()) {
                    if (Objects.equals(pullScreensConfig("first-join-welcome-screen"), true)) {
                        welcomeScreenInitial(p);
                        return;
                    }
                }
                // Display the screen once per restart
                if (Objects.equals(pullScreensConfig("once-per-restart"), true)) {
                    // Check if the player has already seen the screen this server session
                    if (Gizmo.playerTracker.get(p.getUniqueId()) == null) {
                        Gizmo.playerTracker.put(p.getUniqueId(), String.valueOf(1));
                        welcomeScreen(p);
                    }
                } else if (Objects.equals(pullScreensConfig("once-per-restart"), false)) {
                    welcomeScreen(p);
                }
                break;
            case DECLINED:
            case FAILED_DOWNLOAD:
                // Debug mode check; if enabled it will still send the player the welcome screen
                if (Objects.equals(plugin.getConfig().getBoolean("debug-mode"), true)) {
                    p.sendMessage(createComponent(getPrefix() + "<#acb5bf>No server resource pack detected and/or debug mode is enabled."));
                    p.sendMessage(createComponent(getPrefix() + "<#acb5bf>Sending welcome screen..."));
                    welcomeScreen(p);
                } else {
                    p.removePotionEffect(PotionEffectType.BLINDNESS);

                    String noPackLoadedMessage = (String) pullMessagesConfig("no-pack-loaded");
                    if (noPackLoadedMessage == null || noPackLoadedMessage.isBlank()) {
                        return;
                    } else {
                        plugin.getMessagesConfig().getStringList("no-pack-loaded").forEach( string -> p.sendMessage(Utilities.createComponent(string)));
                    }
                }
                break;
        }
    }
    
    // Welcome screen
    public static void welcomeScreen(Player e) {

        // Store the player's ID and set the screen to active
        playersScreenActive.put(e.getUniqueId(), true);
        // Store and clear the player's inventory
        saveInv.put(Objects.requireNonNull(e.getPlayer()).getUniqueId(), e.getPlayer().getInventory().getContents());
        e.getPlayer().getInventory().clear();


        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {

            // Begin the screen sequence
            // check if screens.yml enable-welcome-screen = true
            if (Objects.equals(pullScreensConfig("enable-welcome-screen"), true)) {
                    InventoryView screen = e.getPlayer().openInventory(plugin.getServer().createInventory(null, 54, screenTitle()));

                    if (pullScreensConfig("Items") != null) {
                        for (String key : Objects.requireNonNull(plugin.getScreensConfig().getConfigurationSection("Items")).getKeys(false)) {
                            ConfigurationSection keySection = Objects.requireNonNull(plugin.getScreensConfig().getConfigurationSection("Items")).getConfigurationSection(key);
                            assert keySection != null;
                            int slot = keySection.getInt("slot");
                            ItemStack item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(keySection.getString("material")))));
                            ItemMeta meta = item.getItemMeta();
                            if (meta != null) {
                                List<String> loreConfig = keySection.getStringList("lore");
                                if (!loreConfig.isEmpty()) {
                                    List<Component> lore = loreConfig
                                            .stream()
                                            .map(Utilities::createComponent)
                                            .collect(Collectors.toList());
                                    meta.lore(lore);
                                }
                                if (Objects.equals(keySection.getString("hide-flags"), String.valueOf(true))) {
                                    meta.addItemFlags(
                                            ItemFlag.HIDE_ADDITIONAL_TOOLTIP,
                                            ItemFlag.HIDE_ARMOR_TRIM,
                                            ItemFlag.HIDE_ATTRIBUTES,
                                            ItemFlag.HIDE_DESTROYS,
                                            ItemFlag.HIDE_DYE,
                                            ItemFlag.HIDE_ENCHANTS,
                                            ItemFlag.HIDE_PLACED_ON,
                                            ItemFlag.HIDE_UNBREAKABLE
                                    );
                                }
                                meta.setCustomModelData(keySection.getInt("custom-model-data"));
                                meta.displayName(createComponent(keySection.getString("name")));
                                item.setItemMeta(meta);
                            }
                            assert screen != null;
                            screen.setItem(slot, item);
                        }
                    }
            }
        }, plugin.getConfig().getInt("delay"));
    }

    // Welcome screen first join
    public void welcomeScreenInitial(Player e) {

        // Store the player's ID and set the screen to active
        playersScreenActive.put(e.getUniqueId(), true);
        // Store and clear the player's inventory
        saveInv.put(Objects.requireNonNull(e.getPlayer()).getUniqueId(), e.getPlayer().getInventory().getContents());
        e.getPlayer().getInventory().clear();

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {

            // Begin the screen sequence
            // check if screens.yml enable-first-join-welcome-screen = true
            if (Objects.equals(pullScreensConfig("enable-first-join-welcome-screen"), "true")) {

                    InventoryView screen = e.getPlayer().openInventory(plugin.getServer().createInventory(null, 54, screenTitleFirstJoin()));

                    ConfigurationSection firstJoinItems = plugin.getScreensConfig().getConfigurationSection("First-Join-Items");
                    if (firstJoinItems != null) {
                        for (String key : firstJoinItems.getKeys(false)) {
                            ConfigurationSection keySection = firstJoinItems.getConfigurationSection(key);
                            assert keySection != null;
                            int slot = keySection.getInt("slot");
                            ItemStack item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(keySection.getString("material")))));
                            ItemMeta meta = item.getItemMeta();
                            if (meta != null) {
                                List<String> loreConfig = keySection.getStringList("lore");
                                if (!loreConfig.isEmpty()) {
                                    List<Component> lore = loreConfig
                                            .stream()
                                            .map(Utilities::createComponent)
                                            .collect(Collectors.toList());
                                    meta.lore(lore);
                                }
                                if (keySection.getBoolean("hide-flags")) {
                                    meta.addItemFlags(
                                            ItemFlag.HIDE_ADDITIONAL_TOOLTIP,
                                            ItemFlag.HIDE_ARMOR_TRIM,
                                            ItemFlag.HIDE_ATTRIBUTES,
                                            ItemFlag.HIDE_DESTROYS,
                                            ItemFlag.HIDE_DYE,
                                            ItemFlag.HIDE_ENCHANTS,
                                            ItemFlag.HIDE_PLACED_ON,
                                            ItemFlag.HIDE_UNBREAKABLE
                                    );
                                }
                                meta.setCustomModelData(keySection.getInt("custom-model-data"));
                                meta.displayName(createComponent(keySection.getString("name")));
                                item.setItemMeta(meta);
                                assert screen != null;
                                screen.setItem(slot, item);
                            }
                        }
                    }
            }
        }, plugin.getConfig().getInt("delay"));
    }
}
