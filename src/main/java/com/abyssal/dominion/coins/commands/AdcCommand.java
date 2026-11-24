package com.abyssal.dominion.coins.commands;

import com.abyssal.dominion.coins.AbyssalDominionCoins;
import com.abyssal.dominion.coins.config.Config;
import com.abyssaldominion.suite.RpgMessages;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Locale;

/** Per-subcommand authorization; the root and public version never require admin access. */
public final class AdcCommand implements TabExecutor {
    private static final String PERMISSION = "abyssaldominioncoins.";
    private static final List<String> SUBCOMMANDS = List.of("version", "reload", "balance", "payout");
    private final AbyssalDominionCoins plugin;

    public AdcCommand(AbyssalDominionCoins plugin) { this.plugin = plugin; }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) { help(sender, label); return true; }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (!SUBCOMMANDS.contains(sub) || args.length != 1) {
            RpgMessages.error(sender, "Coins", "Unknown subcommand or invalid arguments: " + args[0]);
            help(sender, label);
            return true;
        }
        if (!sender.hasPermission(PERMISSION + sub)) {
            RpgMessages.error(sender, "Coins", "You do not have permission to use " + sub + ".");
            return true;
        }
        switch (sub) {
            case "version" -> RpgMessages.version(sender, plugin, "Coins");
            case "reload" -> {
                plugin.reload();
                RpgMessages.success(sender, "Coins", plugin.getConfigManager().getMessage("config_reloaded"));
            }
            case "balance" -> {
                if (!(sender instanceof Player player)) {
                    RpgMessages.error(sender, "Coins", "This command can only be used by a player.");
                } else if (!plugin.getEconomyManager().isEconomyEnabled()) {
                    RpgMessages.error(sender, "Coins", "Economy is unavailable.");
                } else {
                    double balance = plugin.getEconomyManager().getBalance(player);
                    if (Double.isFinite(balance)) RpgMessages.send(sender, "Coins", "Your balance: " + balance);
                    else RpgMessages.error(sender, "Coins", "Economy balance could not be read.");
                }
            }
            case "payout" -> {
                Config config = plugin.getConfigManager().getConfig();
                RpgMessages.send(sender, "Coins", "Payout type: " + (config.isBalancePayout() ? "balance" : "flat amount"));
                RpgMessages.send(sender, "Coins", "Flat amount: " + config.getFlatPayout());
            }
            default -> throw new IllegalStateException("Validated subcommand: " + sub);
        }
        return true;
    }
    private void help(CommandSender sender, String label) {
        RpgMessages.heading(sender, "Coins", "Abyssal Dominion Coins");
        String available = String.join("|", SUBCOMMANDS.stream().filter(s -> sender.hasPermission(PERMISSION + s)).toList());
        RpgMessages.send(sender, "Coins", available.isEmpty() ? "No available subcommands." : "Usage: /" + label + " <" + available + ">");
    }
    @Override public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) return List.of();
        String input = args[0].toLowerCase(Locale.ROOT);
        return SUBCOMMANDS.stream().filter(s -> sender.hasPermission(PERMISSION + s) && s.startsWith(input)).toList();
    }
}
