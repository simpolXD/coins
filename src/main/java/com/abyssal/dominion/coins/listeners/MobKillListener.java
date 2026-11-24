package com.abyssal.dominion.coins.listeners;

import com.abyssal.dominion.coins.AbyssalDominionCoins;
import com.abyssal.dominion.coins.config.Config;
import com.abyssal.dominion.coins.utils.FormatUtils;
import com.abyssal.dominion.coins.utils.AntiExploit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Credits coins to the last player who damaged a mob when the mob dies.
 * <p>
 * Uses a WeakHashMap to track the damager per entity, which is thread-safe
 * and survives across event processing threads (unlike ThreadLocal).
 */
public class MobKillListener implements Listener {

    private final AbyssalDominionCoins plugin;
    private final Config config;
    private final AntiExploit antiExploit;

    /** Map of entity -> killer UUID. WeakHashMap prevents memory leaks. */
    private final Map<LivingEntity, UUID> entityDamager = new WeakHashMap<>();

    public MobKillListener(AbyssalDominionCoins plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager().getConfig();
        this.antiExploit = new AntiExploit(plugin);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player p && e.getEntity() instanceof LivingEntity) {
            if (!antiExploit.canProcess(p.getUniqueId(), p.getAddress().getAddress().getHostAddress())) {
                return;
            }
            if (!config.isWorldEnabled(p.getWorld().getName())) {
                return;
            }
            if (!config.isCoinDropsEnabled()) {
                return;
            }
            entityDamager.put((LivingEntity) e.getEntity(), p.getUniqueId());
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        Entity died = e.getEntity();
        if (!(died instanceof LivingEntity)) {
            return;
        }
        LivingEntity entity = (LivingEntity) died;

        UUID damagerId = entityDamager.get(entity);
        if (damagerId == null) {
            return; // No known damager
        }

        Player killer = Bukkit.getPlayer(damagerId);
        if (killer == null || !killer.isOnline()) {
            entityDamager.remove(entity);
            return;
        }

        // Only process if this is the same player who damaged this entity
        if (killer.getUniqueId() != damagerId) {
            entityDamager.remove(entity);
            return;
        }

        processKill(killer, entity);
        entityDamager.remove(entity);
        antiExploit.recordKill(killer.getUniqueId(), killer.getAddress().getAddress().getHostAddress());
    }

    private void processKill(Player killer, LivingEntity entity) {
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

        // Apply ecomobs boss bonus
        if (plugin.getEcomobsHook() != null) {
            double bossMult = plugin.getEcomobsHook().getBossTotalMultiplier(entity, 1.0);
            coins *= bossMult;
        }

        // Apply mob-specific multiplier from ecomobs.yml
        String entityKey = "minecraft:" + entity.getType().getKey().toString();
        if (config.isMobInWhiteList(entityKey)) {
            double mobMult = config.getRandomMobMultiplier(entityKey);
            coins *= mobMult;
        }

        long finalCoins = Math.round(coins);

        // Apply payout
        plugin.getEconomyManager().depositPlayer(killer, finalCoins);

        // Send notification
        String notification = "&a+" + finalCoins + " abyssal coins!";
        if (cfg.isPickupSoundEnabled()) {
            killer.playSound(killer.getLocation(), cfg.getPickupSoundName(), cfg.getPickupSoundPitch(), cfg.getPickupSoundVolume());
        }

        // Send notification using the configured position
        String position = cfg.getPickupNotificationPosition().toLowerCase();
        if (position.contains("title")) {
            FormatUtils.sendTitle(killer, plugin, notification, cfg.getPickupNotificationFormat(), finalCoins);
        } else {
            FormatUtils.sendActionBar(killer, plugin, notification, finalCoins);
        }
    }
}
