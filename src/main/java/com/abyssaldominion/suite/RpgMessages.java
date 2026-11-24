package com.abyssaldominion.suite;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

/** Identical, dependency-free presentation contract embedded by each suite member. */
public final class RpgMessages {
    public static final String AUTHOR = "simp0ol";
    public static final String UNKNOWN_DATE = "Unknown (not recorded)";
    public static final Key FONT = Key.key("minecraft:default");
    public static final TextColor PRIMARY = TextColor.color(0xD4AF37);
    public static final TextColor ACCENT = TextColor.color(0xA78BFA);
    public static final TextColor TEXT = TextColor.color(0xD6D3E0);
    public static final TextColor MUTED = TextColor.color(0x8B819E);
    public static final TextColor SUCCESS = TextColor.color(0x7BC8A4);
    public static final TextColor ERROR = TextColor.color(0xE57373);

    private static final Pattern LEGACY_COLORS = Pattern.compile(
            "(?i)(?:[&§]x(?:[&§][0-9a-f]){6}|[&§]#[0-9a-f]{6}|[&§][0-9a-fk-or])");
    private static final Map<JavaPlugin, Identity> IDENTITIES = Collections.synchronizedMap(new WeakHashMap<>());

    private RpgMessages() {}

    /** Plain text only: user-controlled angle brackets never become MiniMessage tags. */
    public static Component message(String module, String text) {
        return message(module, text, TEXT);
    }

    public static Component message(String module, String text, TextColor color) {
        return Component.empty().font(FONT)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.OBFUSCATED, false)
                .decoration(TextDecoration.BOLD, false)
                .append(Component.text("[", MUTED))
                .append(Component.text("Abyssal Dominion", PRIMARY))
                .append(Component.text(" · ", MUTED))
                .append(Component.text(clean(module), ACCENT))
                .append(Component.text("] ", MUTED))
                .append(Component.text(clean(text), color));
    }

    public static void send(CommandSender sender, String module, String text) {
        sender.sendMessage(message(module, text));
    }

    public static void success(CommandSender sender, String module, String text) {
        sender.sendMessage(message(module, text, SUCCESS));
    }

    public static void error(CommandSender sender, String module, String text) {
        sender.sendMessage(message(module, text, ERROR));
    }

    public static void heading(CommandSender sender, String module, String text) {
        sender.sendMessage(message(module, text, PRIMARY));
    }

    public static void version(CommandSender sender, JavaPlugin plugin, String module) {
        heading(sender, module, plugin.getPluginMeta().getName() + " v" + plugin.getPluginMeta().getVersion());
        send(sender, module, "Author: " + AUTHOR);
        send(sender, module, "Created: " + created(plugin));
        send(sender, module, "Updated: " + updated(plugin));
    }

    public static String created(JavaPlugin plugin) {
        return identity(plugin).created();
    }

    public static String updated(JavaPlugin plugin) {
        return identity(plugin).updated();
    }

    /** Call on disable if the plugin instance is retained by another component. */
    public static void forget(JavaPlugin plugin) {
        IDENTITIES.remove(plugin);
    }

    public static String clean(String text) {
        return text == null ? "" : LEGACY_COLORS.matcher(text).replaceAll("");
    }

    private static Identity identity(JavaPlugin plugin) {
        synchronized (IDENTITIES) {
            return IDENTITIES.computeIfAbsent(plugin, RpgMessages::loadIdentity);
        }
    }

    private static Identity loadIdentity(JavaPlugin plugin) {
        Properties values = new Properties();
        try (InputStream stream = plugin.getResource("suite-info.properties")) {
            if (stream != null) values.load(stream);
        } catch (IOException failure) {
            plugin.getLogger().warning("Unable to read suite-info.properties: " + failure.getMessage());
        }
        return new Identity(value(values, "created"), value(values, "updated"));
    }

    private static String value(Properties values, String key) {
        String value = values.getProperty(key, "").trim();
        return value.isEmpty() ? UNKNOWN_DATE : value;
    }

    private record Identity(String created, String updated) {}
}
