package com.abyssal.dominion.coins;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/** Vault transactions are acknowledged; commands are never used as a pretend economy API. */
public class EconomyManager {
    public static final double MAX_SAFE_AMOUNT = 9_007_199_254_740_991d;
    private final AbyssalDominionCoins plugin;
    private Economy economy;
    private boolean economyEnabled;
    private long lastWarning;

    public EconomyManager(AbyssalDominionCoins plugin) { this.plugin = plugin; }

    public void init() {
        economy = null;
        economyEnabled = false;
        if (!plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
            warn("Vault is not enabled; payouts remain unavailable until a provider is installed.");
            return;
        }
        RegisteredServiceProvider<Economy> registration = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (registration != null && registration.getProvider() != null) {
            economy = registration.getProvider();
            economyEnabled = true;
            plugin.getLogger().info("Vault economy provider found.");
        } else warn("No Vault economy provider is registered; payouts are unavailable.");
    }
    public boolean isEconomyEnabled() { return economyEnabled && economy != null; }

    public boolean depositPlayer(Player player, double amount) {
        if (player == null || !Double.isFinite(amount) || amount <= 0 || amount > MAX_SAFE_AMOUNT || !isEconomyEnabled()) return false;
        try {
            EconomyResponse response = economy.depositPlayer(player, amount);
            if (response != null && response.transactionSuccess()) return true;
            warn("Economy rejected a coin payout" + (response == null ? "." : ": " + response.errorMessage));
        } catch (RuntimeException failure) {
            warn("Economy payout failed: " + failure.getMessage());
        }
        return false;
    }
    /** NaN means unavailable, not a fabricated zero balance. Main-thread callers only. */
    public double getBalance(Player player) {
        if (player == null || !isEconomyEnabled()) return Double.NaN;
        try {
            double balance = economy.getBalance(player);
            return Double.isFinite(balance) ? balance : Double.NaN;
        } catch (RuntimeException failure) {
            warn("Economy balance lookup failed: " + failure.getMessage());
            return Double.NaN;
        }
    }
    private void warn(String message) {
        long now = System.currentTimeMillis();
        if (lastWarning == 0 || now - lastWarning >= 30_000) {
            plugin.getLogger().warning(message);
            lastWarning = now;
        }
    }
}
