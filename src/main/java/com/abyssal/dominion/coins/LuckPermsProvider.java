package com.abyssal.dominion.coins;

import net.luckperms.api.LuckPerms;

import java.lang.reflect.Method;

/** Lazy access to the LuckPerms API instance. */
public class LuckPermsProvider {

    private static LuckPerms luckPerms;

    public static LuckPerms get(org.bukkit.plugin.Plugin plugin) {
        if (luckPerms == null) {
            try {
                // Use reflection to access the LuckPermsProvider API properly
                Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
                Method getProvMethod = providerClass.getMethod("getProv");
                Object provider = getProvMethod.invoke(null);
                Method getProviderMethod = provider.getClass().getMethod("getProvider", org.bukkit.plugin.Plugin.class);
                Object pluginProvider = getProviderMethod.invoke(provider, plugin);
                Method getMethod = pluginProvider.getClass().getMethod("get");
                luckPerms = (LuckPerms) getMethod.invoke(pluginProvider);
            } catch (Exception e) {
                // Fallback: use the static get() method on LuckPermsProvider
                try {
                    Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
                    Method getProvMethod = providerClass.getMethod("get");
                    luckPerms = (LuckPerms) getProvMethod.invoke(null);
                } catch (Exception e2) {
                    // If all else fails, return null
                    luckPerms = null;
                }
            }
        }
        return luckPerms;
    }
}
