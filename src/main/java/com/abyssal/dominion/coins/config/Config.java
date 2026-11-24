package com.abyssal.dominion.coins.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Config section wrapper for Abyssal Dominion Coins v2.0.2.
 * <p>
 * Features:
 * <ul>
 *   <li>Passive/hostile mob drop toggles</li>
 *   <li>Coin amount randomization (min/max range)</li>
 *   <li>Rank multiplier randomization (min/max range)</li>
 *   <li>Notification placement (action bar / title)</li>
 *   <li>Small capital font + purple gradient in lang file</li>
 * </ul>
 */
public class Config {

    private final org.bukkit.configuration.file.FileConfiguration config;
    private FileConfiguration ecomobsConfig;

    public Config(org.bukkit.configuration.file.FileConfiguration config) {
        this.config = config;
    }

    public void setEcomobsConfig(FileConfiguration ecomobsConfig) {
        this.ecomobsConfig = ecomobsConfig;
    }

    // ---- General ----
    public boolean isEnabled() { return config.getBoolean("general.enabled", true); }

    public boolean isCoinDropsEnabled() { return config.getBoolean("general.coin-drops-enabled", true); }

    public boolean isPvPEnabled() { return config.getBoolean("general.pvp-coin-drops-enabled", false); }

    public boolean isSameIpAntiExploitEnabled() { return config.getBoolean("general.anti-exploit.same-ip-enabled", true); }

    public boolean isBalancePayout() { return config.getString("payout-type.type", "balance").equalsIgnoreCase("balance"); }

    public double getBalancePayout() { return isBalancePayout() ? 1.0 : 0.0; }

    public double getFlatPayout() { return config.getDouble("payout-type.flat-amount", 10.0); }

    public int getAntiExploitMaxKillsPerHour() { return config.getInt("general.anti-exploit.max-kills-per-hour", 10); }

    public List<String> getWorldWhitelist() {
        List<String> list = config.getStringList("general.world-whitelist");
        return new ArrayList<>(list);
    }

    public List<String> getWorldBlacklist() {
        List<String> list = config.getStringList("general.world-blacklist");
        return new ArrayList<>(list);
    }

    public boolean isWorldEnabled(String worldName) {
        List<String> wl = getWorldWhitelist();
        List<String> bl = getWorldBlacklist();
        if (!wl.isEmpty() && !wl.contains(worldName)) return false;
        return !bl.contains(worldName);
    }

    // ---- Vanilla mobs ----
    public boolean isVanillaMobDropsEnabled() { return config.getBoolean("vanilla-mobs.enabled", true); }

    public boolean isVanillaHostileDropsEnabled() { return config.getBoolean("vanilla-mobs.hostile.enabled", true); }
    public double getVanillaHostileDropChance() { return config.getDouble("vanilla-mobs.hostile.drop-chance", 0.90); }

    public boolean isVanillaPassiveDropsEnabled() { return config.getBoolean("vanilla-mobs.passive.enabled", false); }
    public double getVanillaPassiveDropChance() { return config.getDouble("vanilla-mobs.passive.drop-chance", 0.50); }
    public double getVanillaMobPercentagePlayerHit() { return config.getDouble("vanilla-mobs.percentage-player-hit", 0.80); }
    public boolean isVanillaPlayerDeathDrop() { return config.getBoolean("vanilla-mobs.player-drop", true); }
    public boolean isVanillaHostileDropEnabledAndChance(double chance) {
        return isVanillaHostileDropsEnabled() && Math.random() < chance;
    }
    public boolean isVanillaPassiveDropEnabledAndChance(double chance) {
        return isVanillaPassiveDropsEnabled() && Math.random() < chance;
    }

    // ---- Coin material ----
    public boolean isCoinMaterialSkull() { return config.getBoolean("coin-material.type.skull", false); }
    public String getCoinMaterialType() { return isCoinMaterialSkull() ? "skull" : "vanilla"; }
    public String getCoinMaterialVanilla() { return config.getString("coin-material.type.vanilla-material", "minecraft:gold_nugget"); }
    public int getCustomModelData() { return config.getInt("coin-material.type.custom-model-data", 0); }
    public double getMinCoinAmount() { return config.getDouble("coin-amount.min", 1.0); }
    public double getMaxCoinAmount() { return config.getDouble("coin-amount.max", 10.0); }
    public double getRandomCoinAmount() {
        double min = getMinCoinAmount();
        double max = getMaxCoinAmount();
        if (min >= max) return min;
        return min + Math.random() * (max - min);
    }
    public double getRandomRankMultiplier() {
        double min = config.getDouble("rank-multipliers.min", 1.0);
        double max = config.getDouble("rank-multipliers.max", 2.0);
        if (min >= max) return min;
        return min + Math.random() * (max - min);
    }
    public double getRankMultiplier(String groupName) {
        double defaultMult = config.getDouble("rank-multipliers.default", 1.0);
        return config.getDouble("rank-multipliers." + groupName.toLowerCase(), defaultMult);
    }

    // ---- Ecomobs ----
    public boolean isEcomobsEnabled() { return config.getBoolean("ecomobs.enabled", true); }
    public double getBossMultiplier() { return config.getDouble("ecomobs.boss-multiplier", 2.0); }
    public double getEntityCoinValue(String key) {
        if (ecomobsConfig != null) {
            Object val = ecomobsConfig.get("entity-coin-value." + key);
            if (val instanceof Number) {
                return ((Number) val).doubleValue();
            }
        }
        return config.getDouble("ecomobs.entity-coin-value." + key, -1.0);
    }

    // ---- Mob config from ecomobs.yml ----
    public boolean isMobInWhiteList(String mobKey) {
        if (ecomobsConfig == null) return false;
        List<String> wl = getVanillaMobWhitelist();
        return wl.contains(mobKey);
    }

    public boolean isMobInBlacklist(String mobKey) {
        if (ecomobsConfig == null) return false;
        List<String> bl = getVanillaMobBlacklist();
        return bl.contains(mobKey);
    }

    public List<String> getVanillaMobWhitelist() {
        List<String> list = new ArrayList<>();
        if (ecomobsConfig != null) {
            list = ecomobsConfig.getStringList("vanilla-mobs.whitelist");
        }
        return new ArrayList<>(list);
    }

    public List<String> getVanillaMobBlacklist() {
        List<String> list = new ArrayList<>();
        if (ecomobsConfig != null) {
            list = ecomobsConfig.getStringList("vanilla-mobs.blacklist");
        }
        return new ArrayList<>(list);
    }

    public double getMobDropChance(String mobKey) {
        if (ecomobsConfig != null) {
            Object chance = ecomobsConfig.get("mob-config." + mobKey + ".drop-chance");
            if (chance instanceof Number) {
                return ((Number) chance).doubleValue();
            }
        }
        return config.getDouble("vanilla-mobs.hostile.drop-chance", 0.90);
    }

    public double getRandomMobMultiplier(String mobKey) {
        double min = getMobMultiplierMin(mobKey);
        double max = getMobMultiplierMax(mobKey);
        if (min >= max) return min;
        return min + Math.random() * (max - min);
    }

    public double getMobMultiplierMin(String mobKey) {
        if (ecomobsConfig != null) {
            Object min = ecomobsConfig.get("mob-config." + mobKey + ".multiplier.min");
            if (min instanceof Number) {
                return ((Number) min).doubleValue();
            }
        }
        return 1.0;
    }

    public double getMobMultiplierMax(String mobKey) {
        if (ecomobsConfig != null) {
            Object max = ecomobsConfig.get("mob-config." + mobKey + ".multiplier.max");
            if (max instanceof Number) {
                return ((Number) max).doubleValue();
            }
        }
        return 1.0;
    }

    // ---- Pickup notification ----
    public boolean isPickupSoundEnabled() { return config.getBoolean("pickup.sound.enabled", true); }
    public String getPickupSoundName() { return config.getString("pickup.sound.name", "minecraft:item.armor.equip_gold"); }
    public float getPickupSoundPitch() { return (float) config.getDouble("pickup.sound.pitch", 0.30); }
    public float getPickupSoundVolume() { return (float) config.getDouble("pickup.sound.volume", 0.50); }
    public String getPickupNotificationPosition() { return config.getString("pickup.notification.position", "actionbar"); }
    public String getPickupNotificationFormat() { return config.getString("pickup.notification.format", "&a+{amount} coins!"); }
    public int getPickupTitleFadeIn() { return config.getInt("pickup.title.fade-in", 10); }
    public int getPickupTitleStay() { return config.getInt("pickup.title.stay", 40); }
    public int getPickupTitleFadeOut() { return config.getInt("pickup.title.fade-out", 10); }

    // ---- Message prefix ----
    public String getMessagePrefix() {
        String prefix = config.getString("message-prefix", "&a[&6AbyssalDominionCoins&c]&a ");
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', prefix);
    }

    public String getLangFile() { return config.getString("lang-file", "lang"); }
    public boolean isLangFileEnabled() { return config.getBoolean("lang-file.enabled", true); }

    // ---- Ore mining ----
    public List<String> getOreWhitelist() {
        List<String> list = config.getStringList("ore-mining.whitelist");
        return new ArrayList<>(list);
    }
    public List<String> getOreBlacklist() {
        List<String> list = config.getStringList("ore-mining.blacklist");
        return new ArrayList<>(list);
    }
    public boolean isOreMiningEnabled() { return config.getBoolean("ore-mining.enabled", true); }
    public boolean isOreMiningEnabledAndChance(double chance) {
        return isOreMiningEnabled() && Math.random() < chance;
    }
    public boolean isOreInWhiteList(String materialKey) {
        List<String> wl = getOreWhitelist();
        return wl.contains(materialKey);
    }
    public boolean isOreInBlacklist(String materialKey) {
        List<String> bl = getOreBlacklist();
        return bl.contains(materialKey);
    }

    // ---- Random coin amount ----
    public int getRandomCoinAmountInt() {
        return (int) Math.round(getRandomCoinAmount());
    }

    // ---- Rank multiplier ----
    public boolean isRankMultiplierEnabled() { return config.getBoolean("rank-multipliers.enabled", true); }
}