package net.jeqo.gizmo.listeners;

import net.jeqo.gizmo.Gizmo;
import net.jeqo.gizmo.data.Utilities;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.jeqo.gizmo.data.Placeholders.getPrefix;
import static net.jeqo.gizmo.data.Placeholders.screenTitle;
import static net.jeqo.gizmo.data.Utilities.*;


public class ScreenAdvance implements Listener {
    final Gizmo plugin = Gizmo.getInstance();

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();

        if (e.getView().title().equals(screenTitle())) {

            if (Objects.equals(plugin.getConfig().getBoolean("enable-fade"), true)) {
                showTitle(p, createComponent(pullConfig("background-color") + (String) pullScreensConfig("Unicodes.background")), null, 0, plugin.getConfig().getInt("fade-time"), 0);
            } else {
                p.clearTitle();
            }


            try {
                if (Objects.equals(plugin.getConfig().getBoolean("sound-on-advance.enable"), true)) {
                    String soundName = plugin.getConfig().getString("sound-on-advance.sound");
                    Registry<@NotNull Sound> soundRegistry = Registry.SOUNDS;
                    Sound sound = soundName != null ? soundRegistry.get(NamespacedKey.minecraft(soundName.toLowerCase())) : null;

                    if (soundName != null && sound != null) {
                        float volume = Float.parseFloat(plugin.getConfig().getString("sound-on-advance.volume", "0.0"));
                        float pitch = Float.parseFloat(plugin.getConfig().getString("sound-on-advance.pitch", "0.0"));
                        if (volume > 0.0 && pitch > 0.0) {
                            p.playSound(p.getLocation(), sound, volume, pitch);
                        }
                    }
                }
            } catch (NullPointerException ex) {
                plugin.getLogger().warning("sound-on-advance is not configured correctly.");
            }

            PlayerScreening.playersScreenActive.remove(p.getUniqueId());

            for (String command : plugin.getConfig().getStringList("commands-on-advance")) {
                if (command.contains("[console]")) {
                    command = command.replace("[console] ", "");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", p.getName()));
                } else if (command.contains("[message]")) {
                    command = command.replace("[message] ", "");
                    p.sendMessage(Utilities.createComponent(command, Placeholder.parsed("player", p.getName())));
                } else if (command.contains("[player]")) {
                    command = command.replace("[player] ", "");
                    p.performCommand(command);
                } else {
                    p.sendMessage(getPrefix() + "An error occurred. Please review the console for more information.");
                    plugin.getLogger().warning("Commands-on-advance (config.yml) has a command with an invalid format.");
                }
            }

            if (!p.hasPlayedBefore()) {
                if (Objects.equals(plugin.getScreensConfig().getBoolean("first-join-welcome-screen"), true)) {
                    welcomeMessageFirstJoin(p);
                }
            } else {
                welcomeMessage(p);
            }
        }
    }

    private void welcomeMessage(Player p) {
        String welcomeMessage = (String) pullMessagesConfig("welcome-message");
        if (welcomeMessage != null && !welcomeMessage.isBlank()) {
            welcomeMessage = welcomeMessage.replaceAll("([|])", "");
            p.sendMessage(createComponent(welcomeMessage));
        }
    }

    private void welcomeMessageFirstJoin(Player p) {
        String welcomeMessage = (String) pullMessagesConfig("first-join-welcome-message");
        if (welcomeMessage != null && !welcomeMessage.isBlank()) {
            welcomeMessage = welcomeMessage.replaceAll("([|])", "");
            p.sendMessage(createComponent(welcomeMessage));
        }
    }
}