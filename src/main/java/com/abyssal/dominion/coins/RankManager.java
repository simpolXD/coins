package com.abyssal.dominion.coins;

import com.abyssal.dominion.coins.config.Config;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.group.Group;

import org.bukkit.Bukkit;

/**
 * LuckPerms rank multiplier manager.
 * Gets the user's highest-ranked group and applies the corresponding multiplier.
 */
public class RankManager {

    private final AbyssalDominionCoins plugin;
    private final EconomyManager economyManager;

    public RankManager(AbyssalDominionCoins plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    public double getMultiplier(org.bukkit.entity.Player player) {
        Config config = plugin.getConfigManager().getConfig();
        if (!config.isRankMultiplierEnabled()) {
            return config.getRandomRankMultiplier();
        }

        net.luckperms.api.LuckPerms luckPerms = LuckPermsProvider.get(Bukkit.getServer().getPluginManager().getPlugin("LuckPerms"));
        if (luckPerms == null) {
            return config.getRandomRankMultiplier();
        }

        net.luckperms.api.model.user.User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return config.getRandomRankMultiplier();
        }

        // Get the highest priority group
        String groupName = user.getPrimaryGroup();
        if (groupName == null) {
            return config.getRandomRankMultiplier();
        }

        // Get the group multiplier from config
        double groupMultiplier = config.getRankMultiplier(groupName);
        if (groupMultiplier < 0) {
            // Group not found in config, use default
            return config.getRandomRankMultiplier();
        }

        return groupMultiplier;
    }
}
