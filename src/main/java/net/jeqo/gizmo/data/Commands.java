package net.jeqo.gizmo.data;

import net.jeqo.gizmo.Gizmo;
import net.jeqo.gizmo.listeners.PlayerScreening;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.jeqo.gizmo.data.Placeholders.*;
import static net.jeqo.gizmo.data.Utilities.*;

public class Commands implements TabExecutor {
    private static final String NUMERIC_REGEX = "[0-9]+";
    private final Gizmo plugin;

    public Commands(Gizmo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {
        if (sender instanceof Player player) {
            if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                displayUsage(player);
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "fade" -> handleFadeCommand(player, args);
                case "reload", "rl" -> handleReloadCommand(player);
                case "show" -> handleShowCommand(player, args);
                default -> player.sendMessage(createComponent(getPrefix() + "<red>Unknown command, use /gizmo help."));
            }
        } else if ("reload".equalsIgnoreCase(args[0])) {
            reloadPluginConfig();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        if (sender.hasPermission("gizmo.reload")) {
            return args.length == 1
                    ? StringUtil.copyPartialMatches(args[0], Arrays.asList("fade", "help", "reload", "show"), new ArrayList<>())
                    : Collections.emptyList();
        }
        return null;
    }

    private void handleFadeCommand(Player player, String[] args) {
        if (!player.hasPermission("gizmo.fade")) {
            sendNoPermission(player);
            return;
        }

        if (args.length >= 4 ) {
            Player target = args.length > 4 ? Bukkit.getPlayer(args[4]) : null;
            executeFadeCommand(player, args, target);
        } else {
            player.sendMessage(createComponent(getPrefix() + "<gray>Usage: /gizmo fade <in> <stay> <out> [player]"));
        }
    }

    private void executeFadeCommand(Player player, String[] args, Player target) {
        if (args[1].matches(NUMERIC_REGEX) && args[2].matches(NUMERIC_REGEX) && args[3].matches(NUMERIC_REGEX)) {
            int fadeIn = Integer.parseInt(args[1]);
            int stay = Integer.parseInt(args[2]);
            int fadeOut = Integer.parseInt(args[3]);

            if (target != null) {
                showTitle(target, screensComponent("Unicodes.background"), Component.empty(), fadeIn, stay, fadeOut);
            } else {
                String background = (String) pullScreensConfig("Unicodes.background");
                showTitle(player, createComponent(background), null, fadeIn, stay, fadeOut);
            }
        } else {
            player.sendMessage(createComponent(getPrefix() + "<red>Only numbers can be used for time values!"));
        }
    }

    private void handleReloadCommand(Player player) {
        if (!player.hasPermission("gizmo.reload")) {
            sendNoPermission(player);
            return;
        }

        reloadPluginConfig();
        String reloadMessage = (String) pullMessagesConfig("config-reloaded");
        if (reloadMessage != null && !reloadMessage.isBlank())
            player.sendMessage(createComponent(getPrefix() + reloadMessage));
    }

    private void handleShowCommand(Player player, String[] args) {
        if (!player.hasPermission("gizmo.show")) {
            sendNoPermission(player);
            return;
        }

        if (args.length <= 1) {
            PlayerScreening.welcomeScreen(player);
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target != null) {
            PlayerScreening.welcomeScreen(target);
            sendPlayerMessageFromConfig(player, "show-screen-others");
        } else {
            sendPlayerMessageFromConfig(player, "player-not-found");
        }
    }

    private void reloadPluginConfig() {
        plugin.reloadConfig();
        plugin.getLogger().info("Config reloaded!");
    }

    @SuppressWarnings("UnstableApiUsage")
    private void displayUsage(CommandSender sender) {
        if (sender.hasPermission("gizmo.reload")) {
            sender.sendMessage(Component.newline());
            sender.sendMessage(createComponent("<red><b>Gizmo</b> <white>" + plugin.getPluginMeta().getVersion() + " by Jeqo"));
            sender.sendMessage(createComponent("<white>/gizmo fade <in> <stay> <out> [player] <gray>- Displays a fade."));
            sender.sendMessage(createComponent("<white>/gizmo reload <gray>- Reloads the Gizmo configs (not recommended)."));
            sender.sendMessage(createComponent("<white>/gizmo show <player> <gray>- Force displays the welcome screen."));
            sender.sendMessage(Component.newline());
        }
    }

    private void sendNoPermission(CommandSender sender) {
        String noPermissionMessage = (String) pullMessagesConfig("no-permission");
        if (noPermissionMessage != null && !noPermissionMessage.isBlank())
            sender.sendMessage(createComponent(getPrefix() + noPermissionMessage));
    }

    private void sendPlayerMessageFromConfig(Player player, String configKey) {
        String message = (String) pullMessagesConfig(configKey);
        if (message != null && !message.isBlank())
            player.sendMessage(createComponent(getPrefix() + message));
    }

}