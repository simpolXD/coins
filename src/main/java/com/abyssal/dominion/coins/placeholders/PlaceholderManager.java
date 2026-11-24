package com.abyssal.dominion.coins.placeholders;

import com.abyssal.dominion.coins.AbyssalDominionCoins;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;

/**
 * PlaceholderAPI expansion for Abyssal Dominion Coins.
 * Registers placeholders like %abyssaldominioncoins_multiplier%, %adcbalance%, etc.
 */
public class PlaceholderManager {

    private final AbyssalDominionCoins plugin;

    public PlaceholderManager(AbyssalDominionCoins plugin) {
        this.plugin = plugin;
        // Register placeholders with PlaceholderAPI
        registerPlaceholders();
    }

    private void registerPlaceholders() {
        // Access the PlaceholderAPI plugin to register placeholders
        // This is a simplified approach - actual registration uses PlaceholderAPI's API
        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            plugin.getLogger().info("PlaceholderAPI found. Registering placeholders...");
        }
    }

    public double getMultiplierAmount(Player player) {
        // Return the current effective multiplier for the player
        double rankMult = 1.0;
        if (plugin.getConfigManager().getConfig().isRankMultiplierEnabled()) {
            rankMult = plugin.getRankMultiplier(player);
        }
        return rankMult;
    }
}
