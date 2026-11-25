package com.abyssal.dominion.coins.listeners;

import com.abyssal.dominion.coins.*;
import com.abyssal.dominion.coins.config.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.junit.jupiter.api.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PickupListenerTest {
    AbyssalDominionCoins plugin; EconomyManager economy; Config config; Player player; Item item; PickupListener listener;
    @BeforeEach void setup() {
        plugin=mock(AbyssalDominionCoins.class); economy=mock(EconomyManager.class); config=mock(Config.class);
        ConfigManager manager=mock(ConfigManager.class);
        when(plugin.getEconomyManager()).thenReturn(economy); when(plugin.getConfigManager()).thenReturn(manager);
        when(manager.getConfig()).thenReturn(config);
        when(config.getCoinMaterialType()).thenReturn("vanilla");when(config.getCoinMaterialVanilla()).thenReturn("minecraft:gold_nugget");
        when(config.getRandomCoinAmountInt()).thenReturn(5);when(config.getPickupNotificationPosition()).thenReturn("actionbar");
        when(config.getPickupNotificationFormat()).thenReturn("&a+{amount} coins!");
        player=mock(Player.class); item=mock(Item.class);when(item.getUniqueId()).thenReturn(UUID.randomUUID());
        var stack=mock(org.bukkit.inventory.ItemStack.class);when(item.getItemStack()).thenReturn(stack);when(stack.getType()).thenReturn(org.bukkit.Material.GOLD_NUGGET);
        listener=new PickupListener(plugin,config,economy);
    }
    @SuppressWarnings("deprecation")
    void pickup(boolean cancelled) throws Exception {
        try {
            var method=PickupListener.class.getMethod("onPickup",EntityPickupItemEvent.class);
            EntityPickupItemEvent event=new EntityPickupItemEvent(player,item,0);event.setCancelled(cancelled);method.invoke(listener,event);
        } catch (NoSuchMethodException legacy) {
            PlayerPickupItemEvent event=new PlayerPickupItemEvent(player,item,0);event.setCancelled(cancelled);
            try { listener.getClass().getMethod("onPickup",PlayerPickupItemEvent.class).invoke(listener,event); }
            catch (java.lang.reflect.InvocationTargetException failure) {
                if (!(failure.getCause() instanceof NullPointerException)) throw failure;
            }
        }
    }
    void track(long value) throws Exception {
        var method=assertDoesNotThrow(()->PickupListener.class.getMethod("track",Item.class,long.class),"Only plugin-issued entity identities may be redeemed");
        method.invoke(listener,item,value);
    }
    @Test void ordinaryGoldNuggetCannotBeRedeemedAsMoney() throws Exception { pickup(false); verify(economy,never()).depositPlayer(any(),anyDouble()); }
    @Test void cancelledPickupCannotCreditMoney() throws Exception { pickup(true); verify(economy,never()).depositPlayer(any(),anyDouble()); }
    @Test void issuedCoinHasFixedValueAndCanOnlyPayOnce() throws Exception {
        track(37);when(economy.depositPlayer(player,37)).thenReturn(true);
        pickup(false);pickup(false);
        verify(economy,times(1)).depositPlayer(player,37);
        verify(item).remove();
    }
    @Test void failedPayoutLeavesCoinAvailableForRetry() throws Exception {
        track(37);when(economy.depositPlayer(player,37)).thenReturn(false,true);
        pickup(false);verify(item,never()).remove();
        pickup(false);verify(item).remove();verify(economy,times(2)).depositPlayer(player,37);
    }
    @Test void reentrantEconomyCallbackCannotRedeemTheSameCoinTwice() throws Exception {
        track(37);
        when(economy.depositPlayer(player,37)).thenAnswer(invocation->{ pickup(false); return true; });
        pickup(false);verify(economy,times(1)).depositPlayer(player,37);
    }
    @Test void closeRemovesOutstandingPhysicalCoins() throws Exception {
        track(37);
        var close=assertDoesNotThrow(()->PickupListener.class.getMethod("close"));close.invoke(listener);
        verify(item).remove();
        pickup(false);verifyNoInteractions(economy);
    }
}
