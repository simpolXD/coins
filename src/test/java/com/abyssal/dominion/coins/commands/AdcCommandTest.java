package com.abyssal.dominion.coins.commands;

import com.abyssal.dominion.coins.AbyssalDominionCoins;
import com.abyssal.dominion.coins.EconomyManager;
import com.abyssal.dominion.coins.config.Config;
import com.abyssal.dominion.coins.config.ConfigManager;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdcCommandTest {
    AbyssalDominionCoins plugin;
    ConfigManager manager;
    Config config;
    AdcCommand handler;
    Command command;
    @BeforeEach void setup() {
        plugin = mock(AbyssalDominionCoins.class);
        manager = mock(ConfigManager.class);
        config = mock(Config.class);
        when(plugin.getConfigManager()).thenReturn(manager);
        when(manager.getConfig()).thenReturn(config);
        when(config.getMessagePrefix()).thenReturn("");
        handler = new AdcCommand(plugin);
        command = mock(Command.class);
    }
    @Test void unprivilegedPlayerCannotReload() {
        handler.onCommand(mock(Player.class), command, "adc", new String[]{"reload"});
        verify(plugin, never()).reload();
    }
    @Test void balanceRequiresItsDeclaredPermission() {
        EconomyManager economy = mock(EconomyManager.class);
        when(plugin.getEconomyManager()).thenReturn(economy);
        handler.onCommand(mock(Player.class), command, "adc", new String[]{"balance"});
        verifyNoInteractions(economy);
    }
    @Test void payoutRequiresItsDeclaredPermission() {
        handler.onCommand(mock(Player.class), command, "adc", new String[]{"payout"});
        verify(plugin, never()).getConfigManager();
    }
    @Test void consoleCanReadPublicVersionWithoutAdminPermission() {
        CommandSender sender = mock(ConsoleCommandSender.class);
        when(sender.hasPermission("abyssaldominioncoins.version")).thenReturn(true);
        PluginMeta meta = mock(PluginMeta.class);
        when(plugin.getPluginMeta()).thenReturn(meta);
        when(meta.getName()).thenReturn("abyssaldominioncoins");
        when(meta.getVersion()).thenReturn("2.1.0");
        assertTrue(handler.onCommand(sender, command, "abyssaldominioncoins", new String[]{"version"}));
        ArgumentCaptor<Component> messages = ArgumentCaptor.forClass(Component.class);
        verify(sender, times(4)).sendMessage(messages.capture());
        List<String> lines=messages.getAllValues().stream().map(PlainTextComponentSerializer.plainText()::serialize).toList();
        assertTrue(lines.get(0).contains("v2.1.0"));
        assertTrue(lines.contains("[Abyssal Dominion · Coins] Author: simp0ol"));
        verify(plugin, never()).reload();
    }
    @Test void explicitVersionPermissionDenialDoesNotExposeMetadata() {
        handler.onCommand(mock(Player.class), command, "adc", new String[]{"version"});
        verify(plugin, never()).getPluginMeta();
    }
    @Test void authorizedConsoleCanReload() {
        CommandSender sender = mock(ConsoleCommandSender.class);
        when(sender.hasPermission("abyssaldominioncoins.reload")).thenReturn(true);
        handler.onCommand(sender, command, "adc", new String[]{"reload"});
        verify(plugin).reload();
    }
    @Test void tabCompletionShowsOnlyAllowedSubcommands() {
        assertInstanceOf(TabCompleter.class, handler);
        Player sender = mock(Player.class);
        when(sender.hasPermission("abyssaldominioncoins.version")).thenReturn(true);
        assertEquals(List.of("version"), ((TabCompleter)handler).onTabComplete(sender, command, "adc", new String[]{""}));
    }
    @Test void consoleBalanceIsSafeAndStyled() {
        CommandSender sender=mock(ConsoleCommandSender.class);
        when(sender.hasPermission("abyssaldominioncoins.balance")).thenReturn(true);
        handler.onCommand(sender,command,"adc",new String[]{"balance"});
        ArgumentCaptor<Component> message=ArgumentCaptor.forClass(Component.class);
        verify(sender).sendMessage(message.capture());
        assertTrue(PlainTextComponentSerializer.plainText().serialize(message.getValue()).contains("player"));
    }
}
