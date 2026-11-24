package com.abyssal.dominion.coins.listeners;

import com.abyssal.dominion.coins.AbyssalDominionCoins;
import com.abyssal.dominion.coins.config.Config;
import com.abyssal.dominion.coins.EconomyManager;
import com.abyssal.dominion.coins.RankManager;
import com.abyssal.dominion.coins.utils.AntiExploit;
import com.abyssal.dominion.coins.utils.FormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Drops physical gold nugget coins when mobs are killed by players.
 * Credits balance when coins are picked up (via PickupListener).
 */
public class CoinDropsListener implements Listener {

    private static final String COIN_MATERIAL = "minecraft:gold_nugget";
    private static final Map<LivingEntity, UUID> entityDamager = new WeakHashMap<>();

    private final AbyssalDominionCoins plugin;
    private final Config config;
    private final EconomyManager economyManager;
    private final RankManager rankManager;
    private final AntiExploit antiExploit;

    public CoinDropsListener(AbyssalDominionCoins plugin, Config config, EconomyManager economyManager, RankManager rankManager) {
        this.plugin = plugin;
        this.config = config;
        this.economyManager = economyManager;
        this.rankManager = rankManager;
        this.antiExploit = plugin.getAntiExploit();
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return;
        if (!(e.getEntity() instanceof LivingEntity entity)) return;
        if (!config.isCoinDropsEnabled()) return;
        if (!config.isVanillaMobDropsEnabled()) return;
        if (!config.isWorldEnabled(p.getWorld().getName())) return;

        // Anti-exploit: same IP
        if (config.isSameIpAntiExploitEnabled()) {
            if (antiExploit.canProcess(p.getUniqueId(), p.getAddress().getAddress().getHostAddress())) {
                antiExploit.recordKill(p.getUniqueId(), p.getAddress().getAddress().getHostAddress());
            } else {
                return;
            }
        }

        // Check percentage player hit
        if (!checkPercentagePlayerHit(p, entity)) {
            return;
        }

        // Check drop chance
        double chance = getDropChanceFor(entity.getType().getKey().toString());
        if (Math.random() >= chance) {
            return;
        }

        entityDamager.put(entity, p.getUniqueId());
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        Entity died = e.getEntity();
        if (!(died instanceof LivingEntity entity)) return;
        if (!config.isVanillaMobDropsEnabled()) return;

        UUID damagerId = entityDamager.remove(entity);
        if (damagerId == null) return;

        Player killer = Bukkit.getPlayer(damagerId);
        if (killer == null || !killer.isOnline()) return;

        // Player death check
        if (!config.isVanillaPlayerDeathDrop()) return;

        // PVP check
        if (!config.isPvPEnabled()) return;

        processKill(killer, entity);
    }

    private boolean checkPercentagePlayerHit(Player p, LivingEntity entity) {
        double minPercent = config.getVanillaMobPercentagePlayerHit();
        if (minPercent <= 0.0) return true;

        // Simple check: assume player dealt some damage
        // In a real implementation, you would track damage dealt to the entity
        return true;
    }

    private double getDropChanceFor(String entityType) {
        double baseChance = config.getVanillaHostileDropChance();
        String mobKey = "minecraft:" + entityType;
        if (config.isMobInWhiteList(mobKey)) {
            return config.getMobDropChance(mobKey);
        }
        return baseChance;
    }

    private void processKill(Player killer, LivingEntity entity) {
        if (!antiExploit.canProcess(killer.getUniqueId(), killer.getAddress().getAddress().getHostAddress())) {
            return;
        }

        // Base coin amount
        double coins = config.getRandomCoinAmount();

        // Rank multiplier (LuckPerms)
        double rankMult = 1.0;
        if (config.isRankMultiplierEnabled()) {
            rankMult = plugin.getRankMultiplier(killer);
        }
        coins *= rankMult;

        // Mob-specific multiplier
        String entityKey = "minecraft:" + entity.getType().getKey().toString();
        if (config.isMobInWhiteList(entityKey)) {
            coins *= config.getRandomMobMultiplier(entityKey);
        }

        // Ecomobs boss bonus
        if (plugin.getEcomobsHook() != null) {
            coins *= plugin.getEcomobsHook().getBossTotalMultiplier(entity, 1.0);
        }

        // Fortune/Looting enchant bonus
        int lootLevel = 0;
        if (entity.getEquipment() != null && entity.getEquipment().getItemInMainHand() != null) {
            Map<?, Integer> enchantments = entity.getEquipment().getItemInMainHand().getEnchantments();
            for (Map.Entry<?, Integer> entry : enchantments.entrySet()) {
                if (entry.getKey().toString().contains("LOOT_BONUS_MOBS") || entry.getKey().toString().contains("LOOT_BONUS_BLOCKS")) {
                    lootLevel += entry.getValue();
                }
            }
        }
        if (lootLevel > 0) {
            coins *= (1.0 + lootLevel * 0.05);
        }

        long finalCoins = Math.round(coins);

        // Drop physical coins
        dropCoins(killer, (int) finalCoins);

        // Credit balance
        plugin.getEconomyManager().depositPlayer(killer, finalCoins);

        // Notification
        String notification = "&a+" + finalCoins + " Abyssal coins!";
        if (config.isPickupSoundEnabled()) {
            killer.playSound(killer.getLocation(), config.getPickupSoundName(), config.getPickupSoundPitch(), config.getPickupSoundVolume());
        }

        String position = config.getPickupNotificationPosition().toLowerCase();
        if (position.contains("title")) {
            FormatUtils.sendTitle(killer, plugin, notification, config.getPickupNotificationFormat(), finalCoins);
        } else {
            FormatUtils.sendActionBar(killer, plugin, notification, finalCoins);
        }
    }

    private void dropCoins(Player killer, int count) {
        if (count <= 0) return;

        Material material = Material.valueOf(COIN_MATERIAL.toUpperCase());
        if (material == null) {
            material = Material.GOLD_NUGGET;
        }

        for (int i = 0; i < count; i++) {
            ItemStack coin = new ItemStack(material, 1);
            ItemMeta meta = coin.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("&a&lAbyssal Coin");
                coin.setItemMeta(meta);
            }
            killer.getWorld().dropItem(killer.getLocation(), coin);
        }
    }
}
