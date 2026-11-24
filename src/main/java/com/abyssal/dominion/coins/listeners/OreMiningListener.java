package com.abyssal.dominion.coins.listeners;

import com.abyssal.dominion.coins.AbyssalDominionCoins;
import com.abyssal.dominion.coins.config.Config;
import com.abyssal.dominion.coins.utils.FormatUtils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Credits coins when ores are mined (configurable whitelist/blacklist).
 */
public class OreMiningListener implements Listener {

    private final AbyssalDominionCoins plugin;
    private final Config config;

    public OreMiningListener(AbyssalDominionCoins plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager().getConfig();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!config.isWorldEnabled(e.getBlock().getWorld().getName())) {
            return;
        }
        if (!config.isOreMiningEnabled()) {
            return;
        }

        String materialKey = "minecraft:" + e.getBlock().getType().name();

        if (!config.isOreInWhiteList(materialKey) && !config.getOreWhitelist().isEmpty()) {
            return;
        }
        if (config.isOreInBlacklist(materialKey)) {
            return;
        }

        if (!config.isOreMiningEnabledAndChance(1.0)) {
            return;
        }

        plugin.getEconomyManager().depositPlayer(e.getPlayer(), 1);
        FormatUtils.sendActionBar(e.getPlayer(), plugin, "&aYou mined an ore and received 1 coin.", 1);
    }
}
