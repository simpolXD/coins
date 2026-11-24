package com.abyssal.dominion.coins;

import com.abyssal.dominion.coins.commands.AdcCommand;
import com.abyssal.dominion.coins.config.ConfigManager;
import com.abyssal.dominion.coins.config.Config;
import com.abyssal.dominion.coins.ecomobs.EcomobsHook;
import com.abyssal.dominion.coins.listeners.CoinDropsListener;
import com.abyssal.dominion.coins.listeners.OreMiningListener;
import com.abyssal.dominion.coins.listeners.PickupListener;
import com.abyssal.dominion.coins.listeners.PvpKillListener;
import com.abyssal.dominion.coins.utils.AntiExploit;
import com.abyssal.dominion.coins.EconomyManager;
import com.abyssal.dominion.coins.RankManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Abyssal Dominion Coins v2.0.4 - Modern Paper 26.2 plugin.
 * Drives physical coin items on mob kills, PVP, ore mining, with LuckPerms
 * rank multiplier, PlaceholderAPI, Vault economy, and full config-driven control.
 *
 * Drop mechanics:
 * - Kills a mob -> drops physical gold nugget coins (or gold ingot)
 * - On pickup -> adds value to player balance (Vault/EssentialsX)
 * - Multiplier via LuckPerms rank groups
 * - Config: hostile/passive drop toggles, percentage-player-hit (min % damage),
 *   drop-chance, mob-multiplier, enchant-increment, anti-exploit, location limits
 */
public final class AbyssalDominionCoins extends JavaPlugin {

    private ConfigManager configManager;
    private EconomyManager economyManager;
    private RankManager rankManager;
    private EcomobsHook ecomobsHook;
    private AntiExploit antiExploit;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.load();

        // ---- Economy (Vault / EssentialsX) ----
        economyManager = new EconomyManager(this);
        economyManager.init();

        // ---- LuckPerms ----
        if (getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            rankManager = new RankManager(this, economyManager);
        } else {
            rankManager = null;
            getLogger().warning("LuckPerms not enabled! Rank multiplier disabled.");
        }

        // ---- Ecomobs hook ----
        if (configManager.getConfig().isEcomobsEnabled()) {
            ecomobsHook = new EcomobsHook(this);
            ecomobsHook.enable();
        }

        // ---- Listeners ----
        // ---- Anti-exploit ----
        antiExploit = new AntiExploit(this);
        if (configManager.getConfig().isSameIpAntiExploitEnabled()) {
            antiExploit.init();
        }

        CoinDropsListener coinListener = new CoinDropsListener(this, configManager.getConfig(), economyManager, rankManager);
        OreMiningListener oreListener = new OreMiningListener(this);
        PickupListener pickupListener = new PickupListener(this, configManager.getConfig(), economyManager);
        PvpKillListener pvpListener = new PvpKillListener(this);

        getServer().getPluginManager().registerEvents(coinListener, this);
        getServer().getPluginManager().registerEvents(oreListener, this);
        getServer().getPluginManager().registerEvents(pickupListener, this);
        if (configManager.getConfig().isPvPEnabled()) {
            getServer().getPluginManager().registerEvents(pvpListener, this);
        }

        // ---- Commands ----
        AdcCommand adminCommand = new AdcCommand(this);
        getCommand("adc").setExecutor(adminCommand);

        // ---- Placeholders ----
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new com.abyssal.dominion.coins.placeholders.PlaceholderManager(this);
        }

        getLogger().info("Abyssal Dominion Coins v2.0.5 enabled.");
    }

    @Override
    public void onDisable() {
        if (ecomobsHook != null) {
            ecomobsHook.disable();
        }
    }

    public ConfigManager getConfigManager() { return configManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public RankManager getRankManager() { return rankManager; }
    public EcomobsHook getEcomobsHook() { return ecomobsHook; }
    public AntiExploit getAntiExploit() { return antiExploit; }

    public void reload() {
        configManager.reload();
    }

    public double getRankMultiplier(org.bukkit.entity.Player player) {
        if (rankManager == null) return 1.0;
        return rankManager.getMultiplier(player);
    }
}
