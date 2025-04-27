package net.jeqo.gizmo.data;

import me.clip.placeholderapi.PlaceholderAPI;
import net.jeqo.gizmo.Gizmo;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class Utilities {
    static final Gizmo plugin = Gizmo.getInstance();

    public static Component createComponent(String string, TagResolver... resolvers) {
        if (string == null) return Component.empty();
        return MiniMessage.miniMessage().deserialize(string, resolvers);
    }

    public static Component createComponent(Player player, String string, TagResolver... resolvers) {
        if (string == null) return Component.empty();
        if (player != null) {
            string = PlaceholderAPI.setPlaceholders(player, string);
        }
        return createComponent(string, resolvers);
    }

    public static Component screensComponent(String id) {
        return pullScreensConfig(id) instanceof String ? createComponent((String) pullScreensConfig(id)) : Component.empty();

    }

    public static Object pullConfig(String id) {
        return plugin.getConfig().contains(id) ? plugin.getConfig().get(id) : null;
    }

    public static Object pullScreensConfig(String id) {
        return plugin.getScreensConfig().contains(id) ? plugin.getScreensConfig().get(id) : null;
    }

    public static Object pullMessagesConfig(String id) {
        return plugin.getMessagesConfig().contains(id) ? plugin.getMessagesConfig().get(id) : null;
    }

    public static void showTitle(final Audience target, @Nullable Component mainTitle, @Nullable Component subtitle, int fadeIn, int stay, int fadeOut) {
        if (!(target instanceof Player)) return;

        if (mainTitle == null) mainTitle = Component.empty();
        if (subtitle == null) subtitle = Component.empty();

        final Title.Times times = Title.Times.times(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut));
        final Title title = Title.title(mainTitle, subtitle, times);

        target.showTitle(title);
    }
}
