package com.abyssal.dominion.coins.config;

import com.abyssal.dominion.coins.AbyssalDominionCoins;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * Manages loading and saving of the plugin's config.yml, lang.yml, and ecomobs.yml.
 */
public class ConfigManager {

    private final AbyssalDominionCoins plugin;
    private File configFile;
    private File langFile;
    private File ecomobsFile;
    private Config config;
    private FileConfiguration ecomobsConfig;

    public ConfigManager(AbyssalDominionCoins plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.langFile = new File(plugin.getDataFolder(), "lang.yml");
        this.ecomobsFile = new File(plugin.getDataFolder(), "ecomobs.yml");
    }

    public void load() {
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        plugin.reloadConfig();
        this.config = new Config(plugin.getConfig());

        if (!langFile.exists()) {
            plugin.saveResource("lang.yml", false);
        }

        if (!ecomobsFile.exists()) {
            plugin.saveResource("ecomobs.yml", false);
        }
        ecomobsConfig = YamlConfiguration.loadConfiguration(ecomobsFile);
        config.setEcomobsConfig(ecomobsConfig);
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = new Config(plugin.getConfig());
        if (langFile.exists()) {
            YamlConfiguration.loadConfiguration(langFile);
        }
        if (ecomobsFile.exists()) {
            ecomobsConfig = YamlConfiguration.loadConfiguration(ecomobsFile);
            config.setEcomobsConfig(ecomobsConfig);
        }
        if (plugin.getEcomobsHook() != null) {
            plugin.getEcomobsHook().reload();
        }
        plugin.getLogger().info("Config, lang, and ecomobs reloaded.");
    }

    public File getConfigFile() { return configFile; }
    public File getLangFile() { return langFile; }
    public File getEcomobsFile() { return ecomobsFile; }
    public Config getConfig() { return config; }
    public FileConfiguration getLangConfiguration() {
        return YamlConfiguration.loadConfiguration(langFile);
    }
    public FileConfiguration getEcomobsConfiguration() {
        return ecomobsConfig;
    }

    public String getMessage(String key, String... replacements) {
        FileConfiguration lang = getLangConfiguration();
        String message = lang.getString("messages." + key, "§c[missing]");
        for (int i = 0; i < replacements.length; i++) {
            message = message.replace("{" + i + "}", replacements[i]);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
