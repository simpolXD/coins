package com.abyssal.dominion.coins.utils;

import com.abyssal.dominion.coins.AbyssalDominionCoins;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * Formatting utilities with purple gradient style.
 * Uses Adventure API (Paper 1.19+) with MiniMessage.
 * Falls back to legacy color codes for older Paper versions.
 */
public class FormatUtils {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    private FormatUtils() {}

    /** Purple gradient colors for small caps + purple gradient style. */
    private static final String[] purpleColors = {
        "\u00a79", // bright purple
        "\u00a75", // dark purple
        "\u00a7d", // light purple
        "\u00a78", // dark blue-purple
        "\u00a76", // purple
        "\u00a74", // blue
        "\u00a7b", // light purple
        "\u00a79", // bright purple
    };

    private static org.bukkit.Color parseColor(String hex) {
        if (hex == null || hex.length() < 6) return org.bukkit.Color.fromRGB(255, 255, 255);
        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return org.bukkit.Color.fromRGB(r, g, b);
        } catch (Exception e) {
            return org.bukkit.Color.fromRGB(100, 0, 255); // Default purple
        }
    }

    /**
     * Send an action bar notification above the hotbar.
     * Purple gradient style with small caps.
     */
    public static void sendActionBar(Player player, AbyssalDominionCoins plugin, String message, long amount) {
        String prefix = plugin.getConfigManager().getConfig().getMessagePrefix();
        String fullMessage = prefix != null ? prefix + message : message;
        String processed = processMessage(fullMessage, amount);
        if (player.hasMetadata("adventure-module")) {
            // Adventure API: build Component and send
            Component component = miniMessage.deserialize(processed);
            player.sendMessage(component);
        } else {
            // Adventure API works on modern Paper
            player.sendMessage(net.kyori.adventure.text.Component.text(processed));
        }
    }

    /**
     * Send a title notification to the player.
     * Purple gradient style with {amount} replacement.
     */
    public static void sendTitle(Player player, AbyssalDominionCoins plugin, String title, String format, long amount) {
        String formattedTitle = processMessage(title, amount);
        String formattedSubtitle = format != null ? processMessage(format, amount) : null;
        player.sendTitle(formattedTitle, formattedSubtitle);
    }

    private static String processMessage(String message, long amount) {
        if (message == null || message.isEmpty()) return message;

        // Apply small caps
        message = message.replace("&l", "§l").replace("&L", "§l");

        // Translate legacy color codes
        String colored = ChatColor.translateAlternateColorCodes('&', message);

        // Apply purple gradient based on character position
        String gradient = applyGradient(colored);

        // Replace {amount} placeholder
        String result = gradient.replace("{amount}", String.valueOf(amount));

        // Ensure message ends with a color code
        if (result != null && result.length() > 0 && !result.endsWith("§")) {
            result += "§f";
        }

        return result;
    }

    private static String applyGradient(String message) {
        if (message == null || message.isEmpty()) return message;

        String[] purpleColors = {
                "\u00a74", // dark purple
                "\u00a79", // purple
                "\u00a76", // gold-purple
                "\u00a75", // dark purple
                "\u00a7a"  // green
        };

        StringBuilder result = new StringBuilder();
        char[] chars = message.toCharArray();
        int colorIndex = 0;

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (c == '\u00a7') {
                // Skip existing color codes
                if (i + 1 < chars.length) {
                    char next = chars[i + 1];
                    if (next == 'l' || next == 'r' || next == 'o' || next == '0' ||
                            (next >= '1' && next <= '9') || next == 'k' || next == 'm') {
                        result.append("§").append(next);
                        i++;
                        continue;
                    }
                }
                continue;
            }

            // Apply purple gradient based on character
            int pos = 0;
            if (c >= '0' && c <= '9') {
                pos = c - '0';
            } else if (c >= 'A' && c <= 'Z') {
                pos = c - 'A' + 10;
            } else if (c >= 'a' && c <= 'z') {
                pos = c - 'a' + 10;
            }

            if (pos >= 0 && pos < purpleColors.length) {
                result.append(purpleColors[pos]);
            } else {
                result.append(purpleColors[purpleColors.length - 1]);
            }
        }

        // Ensure ends with color
        if (message.length() > 0 && !message.endsWith("§")) {
            result.append(purpleColors[purpleColors.length - 1]);
        }

        return result.toString();
    }
}
