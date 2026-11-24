package com.abyssal.dominion.coins.listeners;

import com.abyssal.dominion.coins.AbyssalDominionCoins;
import com.abyssal.dominion.coins.EconomyManager;
import com.abyssal.dominion.coins.config.Config;
import com.abyssal.dominion.coins.utils.FormatUtils;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityPickupItemEvent;
import java.util.*;

/** Main-thread entity-identity ledger. An ordinary or copied item stack is never currency. */
public final class PickupListener implements Listener, AutoCloseable {
    private final AbyssalDominionCoins plugin;
    private final EconomyManager economy;
    private final Map<UUID, Claim> claims = new HashMap<>();
    public PickupListener(AbyssalDominionCoins plugin, Config ignored, EconomyManager economy) {
        this.plugin = plugin; this.economy = economy;
    }
    /** Only call for an item entity just spawned by this plugin. Value does not depend on pickup configuration. */
    public void track(Item item, long value) {
        if (value <= 0 || value > EconomyManager.MAX_SAFE_AMOUNT) throw new IllegalArgumentException("Invalid coin value");
        if (claims.containsKey(item.getUniqueId())) throw new IllegalArgumentException("Entity is already a coin");
        item.setPersistent(false);
        claims.put(item.getUniqueId(), new Claim(item, value));
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.isCancelled()) return;
        Claim claim = claims.get(event.getItem().getUniqueId());
        if (claim == null) return;
        event.setCancelled(true); // Never transfer currency into a physical inventory.
        if (!(event.getEntity() instanceof Player player) || claim.busy || claim.paid) return;
        claim.busy = true;
        try {
            if (!economy.depositPlayer(player, claim.value)) return;
            claim.paid = true; // Set before removal/notifications, including re-entrant event callbacks.
            claim.item.remove();
            claims.remove(claim.item.getUniqueId(), claim);
            Config config = plugin.getConfigManager().getConfig();
            String text = config.getPickupNotificationFormat();
            if (config.getPickupNotificationPosition().equalsIgnoreCase("title"))
                FormatUtils.sendTitle(player, plugin, text, null, claim.value);
            else FormatUtils.sendActionBar(player, plugin, text, claim.value);
        } finally { claim.busy = false; }
    }
    @Override public void close() {
        List<Claim> outstanding = List.copyOf(claims.values());
        claims.clear();
        outstanding.forEach(claim -> claim.item.remove());
    }
    private static final class Claim {
        final Item item; final long value; boolean busy; boolean paid;
        Claim(Item item, long value) { this.item = item; this.value = value; }
    }
}
