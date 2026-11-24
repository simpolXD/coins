package com.abyssal.dominion.coins.listeners;

import com.abyssal.dominion.coins.AbyssalDominionCoins;
import com.abyssal.dominion.coins.config.Config;
import com.abyssal.dominion.coins.utils.FormatUtils;
import com.abyssal.dominion.coins.utils.AntiExploit;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.UUID;

/**
 * Credits coins to the last player who killed another player (PVP).
 */
public class PvpKillListener implements Listener {

    private final AbyssalDominionCoins plugin;
    private final Config config;
    private final AntiExploit antiExploit;

    private final ThreadLocal<UUID> lastPvpDamagerKey = new ThreadLocal<>();

    public PvpKillListener(AbyssalDominionCoins plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager().getConfig();
        this.antiExploit = new AntiExploit(plugin);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player p && e.getEntity() instanceof LivingEntity) {
            if (!config.isPvPEnabled()) {
                return;
            }
            if (!antiExploit.canProcess(p.getUniqueId(), p.getAddress().getAddress().getHostAddress())) {
                return;
            }
            if (!config.isWorldEnabled(p.getWorld().getName())) {
                return;
            }
            lastPvpDamagerKey.set(p.getUniqueId());
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (!config.isPvPEnabled()) {
            return;
        }

        Player killer = Bukkit.getPlayer(lastPvpDamagerKey.get());
        if (killer == null || !killer.isOnline()) {
            lastPvpDamagerKey.remove();
            return;
        }

        if (killer.isOnline()) {
            processKill(killer);
        }
        lastPvpDamagerKey.remove();
    }

    private void processKill(Player killer) {
        Config cfg = plugin.getConfigManager().getConfig();

        if (!antiExploit.canProcess(killer.getUniqueId(), killer.getAddress().getAddress().getHostAddress())) {
            return;
        }

        // Base coin amount (random from min to max)
        double coins = cfg.getRandomCoinAmount();

        // Apply rank multiplier
        double rankMult = 1.0;
        if (cfg.isRankMultiplierEnabled()) {
            rankMult = plugin.getRankMultiplier(killer);
        }
        coins *= rankMult;

        long finalCoins = Math.round(coins);

        // Apply payout
        plugin.getEconomyManager().depositPlayer(killer, finalCoins);

        // Send notification
        String notification = "&a+" + finalCoins + " abyssal coins!";
        if (cfg.isPickupSoundEnabled()) {
            killer.playSound(killer.getLocation(), cfg.getPickupSoundName(), cfg.getPickupSoundPitch(), cfg.getPickupSoundVolume());
        }

        // Use notification position from config
        String position = cfg.getPickupNotificationPosition().toLowerCase();
        if (position.contains("title")) {
            FormatUtils.sendTitle(killer, plugin, notification, cfg.getPickupNotificationFormat(), finalCoins);
        } else {
            FormatUtils.sendActionBar(killer, plugin, notification, finalCoins);
        }
    }
}
