package com.abyssal.dominion.coins;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EconomyManagerTest {
    EconomyManager manager;
    Economy provider;
    Player player;
    @BeforeEach void setup() throws Exception {
        AbyssalDominionCoins plugin=mock(AbyssalDominionCoins.class);
        when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());
        manager=new EconomyManager(plugin);
        provider=mock(Economy.class);
        player=mock(Player.class);
        set("economy",provider);
        set("economyEnabled",true);
    }
    void set(String field,Object value) throws Exception {
        var f=EconomyManager.class.getDeclaredField(field);f.setAccessible(true);f.set(manager,value);
    }
    Object credit(double amount) throws Exception {
        return EconomyManager.class.getMethod("depositPlayer",Player.class,double.class).invoke(manager,player,amount);
    }
    @ParameterizedTest @ValueSource(doubles={-1,0,Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.MAX_VALUE})
    void invalidAmountsNeverReachProvider(double amount) throws Exception {
        credit(amount);
        verifyNoInteractions(provider);
    }
    @Test void failedTransactionIsReportedAsFailure() throws Exception {
        when(provider.depositPlayer(player,7)).thenReturn(new EconomyResponse(0,0,EconomyResponse.ResponseType.FAILURE,"insufficient funds"));
        assertEquals(Boolean.FALSE,credit(7));
    }
    @Test void successIsReportedOnlyAfterProviderAcknowledges() throws Exception {
        when(provider.depositPlayer(player,7)).thenReturn(new EconomyResponse(7,7,EconomyResponse.ResponseType.SUCCESS,null));
        assertEquals(Boolean.TRUE,credit(7));
    }
    @Test void unavailableBalanceIsNotFabricatedAsZero() throws Exception {
        set("economyEnabled",false);
        assertTrue(Double.isNaN(manager.getBalance(player)));
    }
    @Test void providerExceptionsFailClosed() throws Exception {
        when(provider.depositPlayer(player,7)).thenThrow(new IllegalStateException("provider down"));
        assertEquals(Boolean.FALSE,credit(7));
    }
}
