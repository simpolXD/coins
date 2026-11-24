package com.abyssaldominion.suite;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RpgMessagesTest {
    private String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Test void commandMessageUsesCommonPrefixDefaultFontAndNoItalics() {
        Component message = RpgMessages.message("Pets", "Your companion awaits.");
        assertEquals("[Abyssal Dominion · Pets] Your companion awaits.", plain(message));
        assertEquals(Key.key("minecraft:default"), message.font());
        assertEquals(TextDecoration.State.FALSE, message.decoration(TextDecoration.ITALIC));
        assertEquals(TextDecoration.State.FALSE, message.decoration(TextDecoration.OBFUSCATED));
    }

    @Test void playerSuppliedMiniMessageTagsAreLiteralNotMarkup() {
        assertTrue(plain(RpgMessages.message("Trade", "<red>not markup</red>")).contains("<red>not markup</red>"));
    }

    @Test void legacyConfiguredColorsAreStrippedWithoutDamagingAmpersands() {
        assertEquals("[Abyssal Dominion · Coins] Gold & Silver ready", plain(RpgMessages.message("Coins", "&6Gold & Silver §aready")));
        assertEquals("[Abyssal Dominion · Pets] Pet", plain(RpgMessages.message("Pets", "&#A78BFAPet")));
    }

    @Test void successAndErrorUseSamePrefix() {
        CommandSender sender = mock(CommandSender.class);
        RpgMessages.success(sender, "Food Effects", "The tome is renewed.");
        RpgMessages.error(sender, "Food Effects", "Access denied.");
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(sender, times(2)).sendMessage(captor.capture());
        assertTrue(captor.getAllValues().stream().allMatch(c -> plain(c).startsWith("[Abyssal Dominion · Food Effects] ")));
    }

    @Test void identityUsesPropertiesAndNeverFabricatesMissingCreationDate() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getResource("suite-info.properties")).thenReturn(new ByteArrayInputStream("created=Unknown (not recorded)\nupdated=2026-11-06\n".getBytes(StandardCharsets.UTF_8)));
        assertEquals("Unknown (not recorded)", RpgMessages.created(plugin));
        assertEquals("2026-11-06", RpgMessages.updated(plugin));
        assertEquals("simp0ol", RpgMessages.AUTHOR);
    }

    @Test void missingIdentityResourceUsesHonestFallback() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        assertEquals("Unknown (not recorded)", RpgMessages.created(plugin));
        assertEquals("Unknown (not recorded)", RpgMessages.updated(plugin));
    }

    @Test void versionOutputReadsActualPluginMetadataAndIncludesAttributionAndDates() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        io.papermc.paper.plugin.configuration.PluginMeta meta = mock(io.papermc.paper.plugin.configuration.PluginMeta.class);
        when(plugin.getPluginMeta()).thenReturn(meta);
        when(meta.getName()).thenReturn("AbyssalDominionExample");
        when(meta.getVersion()).thenReturn("9.8.7-test");
        when(plugin.getResource("suite-info.properties")).thenReturn(new ByteArrayInputStream("created=Unknown (not recorded)\nupdated=2026-11-06\n".getBytes(StandardCharsets.UTF_8)));
        CommandSender sender = mock(CommandSender.class);
        RpgMessages.version(sender, plugin, "Example");
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(sender, times(4)).sendMessage(captor.capture());
        java.util.List<String> messages = captor.getAllValues().stream().map(this::plain).toList();
        assertEquals("[Abyssal Dominion · Example] AbyssalDominionExample v9.8.7-test", messages.get(0));
        assertEquals("[Abyssal Dominion · Example] Author: simp0ol", messages.get(1));
        assertEquals("[Abyssal Dominion · Example] Created: Unknown (not recorded)", messages.get(2));
        assertEquals("[Abyssal Dominion · Example] Updated: 2026-11-06", messages.get(3));
    }

    @Test void identityRequestPathReadsResourceOnlyOnceAndCanBeClearedOnDisable() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getResource("suite-info.properties")).thenAnswer(invocation -> new ByteArrayInputStream("created=Unknown (not recorded)\nupdated=2026-11-06\n".getBytes(StandardCharsets.UTF_8)));
        RpgMessages.created(plugin);
        RpgMessages.updated(plugin);
        RpgMessages.created(plugin);
        verify(plugin, times(1)).getResource("suite-info.properties");
        RpgMessages.forget(plugin);
        RpgMessages.created(plugin);
        verify(plugin, times(2)).getResource("suite-info.properties");
    }
}
