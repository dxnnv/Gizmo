package net.jeqo.gizmo.data;

import net.jeqo.gizmo.Gizmo;
import net.kyori.adventure.text.Component;

import static net.jeqo.gizmo.data.Utilities.*;

public class Placeholders {
    static final Gizmo plugin = Gizmo.getInstance();

    private static final String DEFAULT_PREFIX = "<#ee0000>[Gizmo] ";

    public static String getPrefix() {
        if (plugin == null) return DEFAULT_PREFIX;

        String prefix = plugin.getConfig().getString("prefix", DEFAULT_PREFIX);
        return prefix.isBlank() ? DEFAULT_PREFIX : prefix;
    }

    public static String shift1013() {
        return plugin.getScreensConfig().getString("Unicodes.shift-1013");
    }
    public static String shift1536() {
        return plugin.getScreensConfig().getString("Unicodes.shift-1536");
    }

    public static Component screenTitle() {
        return createComponent(plugin.getConfig().getString("background-color") + shift1013() + pullScreensConfig("Unicodes.background") + shift1536() + "<white>" + pullScreensConfig("Unicodes.welcome-screen"));
    }
    public static Component screenTitleFirstJoin() {
        return createComponent(plugin.getConfig().getString("background-color") + shift1013() + pullScreensConfig("Unicodes.first-join-background") + shift1536() + "<white>" + pullScreensConfig("Unicodes.first-join-welcome-screen"));
    }
}
