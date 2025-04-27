package net.jeqo.gizmo.listeners;

import net.jeqo.gizmo.Gizmo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Objects;

import static net.jeqo.gizmo.data.Placeholders.getPrefix;
import static net.jeqo.gizmo.data.Placeholders.screenTitle;
import static net.jeqo.gizmo.data.Utilities.createComponent;

public class ClickableItems implements Listener {
    final Gizmo plugin = Gizmo.getInstance();

    @EventHandler
    public void onCommandItemClick(InventoryClickEvent e) {

        if (e.getView().title().equals(screenTitle())) {

            Player player = (Player) e.getWhoClicked();
            int rawSlot = e.getRawSlot();

            for (String key : Objects.requireNonNull(plugin.getScreensConfig().getConfigurationSection("Items")).getKeys(false)) {
                if (plugin.getScreensConfig().getInt("Items." + key + ".slot") == rawSlot) {
                    if (plugin.getScreensConfig().getString("Items." + key + ".commands") != null) {
                        if (plugin.getScreensConfig().getBoolean("Items." + key + ".close-on-click")) {
                            player.closeInventory();
                        }
                        List<String> commands = plugin.getScreensConfig().getStringList("Items." + key + ".commands");
                        processItemCommands(key, commands, player);
                    }
                }
            }
        }
    }

    private void executeCommand(String command, Player player, String key) {
        String playerPrefix = "[player]";
        String messagePrefix = "[message]";
        String consolePrefix = "[console]";

        if (command.startsWith(consolePrefix)) {
            String cmd = command.substring(consolePrefix.length() + 1).replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        } else if (command.startsWith(messagePrefix)) {
            String message = command.substring(messagePrefix.length() + 1).replace("%player%", player.getName());
            player.sendMessage(createComponent(message));
        } else if (command.startsWith(playerPrefix)) {
            String playerCommand = command.substring(playerPrefix.length() + 1);
            player.performCommand(playerCommand);
        } else {
            player.sendMessage(createComponent(getPrefix() + "An error occurred. Please review the console for more information."));
            plugin.getLogger().warning("\"" + key + "\" (screens.yml) has a command with an invalid format.");
        }
    }

    private void processItemCommands(String key, List<String> commands, Player player) {
        commands.forEach(command -> executeCommand(command, player, key));
    }
}
