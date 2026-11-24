package com.abyssal.dominion.coins.ecomobs;

import com.abyssal.dominion.coins.AbyssalDominionCoins;
import com.abyssal.dominion.coins.config.Config;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/** Ecomobs boss hook - applies additional coin values for ecomobs boss mobs. */
public class EcomobsHook {

    private final AbyssalDominionCoins plugin;
    private final Config config;

    public EcomobsHook(AbyssalDominionCoins plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager().getConfig();
    }

    public void reload() {
        // Reload happens automatically with config reload
    }

    public void enable() {
        // Ecomobs features are enabled via config
        if (!config.isEcomobsEnabled()) {
            // Disable any hook features
        }
    }

    public void disable() {
        // No resources to clean up
    }

    /** Checks if the given entity is a registered ecomobs boss and returns the extra coins to add. */
    public double getBossExtraCoins(LivingEntity entity) {
        if (entity == null) return 0.0;

        String entityKey = entity.getType().getKey().toString();
        if (entityKey == null) return 0.0;

        // Check if this entity is in the ecomobs entity-coin-value map
        double customValue = config.getEntityCoinValue(entityKey);
        if (customValue > 0) {
            return customValue;
        }

        // If no custom value, use the boss multiplier for all bosses
        if (config.isEcomobsEnabled()) {
            return 0.0;
        }

        return 0.0;
    }

    /** Gets the total coin multiplier for a boss entity (boss multiplier + rank multiplier). */
    public double getBossTotalMultiplier(LivingEntity entity, double baseMultiplier) {
        double extra = getBossExtraCoins(entity);
        if (extra > 0) {
            return baseMultiplier * config.getBossMultiplier();
        }
        return baseMultiplier;
    }
}
