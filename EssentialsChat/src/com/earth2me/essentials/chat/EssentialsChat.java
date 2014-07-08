package com.earth2me.essentials.chat;

import net.ess3.api.IEssentials;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class EssentialsChat extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        final PluginManager pluginManager = getServer().getPluginManager();
        final IEssentials ess = (IEssentials)pluginManager.getPlugin("Essentials");

        if (!ess.isEnabled())
        {
            this.setEnabled(false);
            return;
        }

        final Map<AsyncPlayerChatEvent, ChatStore> chatStore = Collections.synchronizedMap(new HashMap<>());

        final EssentialsChatPlayerListenerLowest playerListenerLowest = new EssentialsChatPlayerListenerLowest(getServer(), ess, chatStore);
        final EssentialsChatPlayerListenerNormal playerListenerNormal = new EssentialsChatPlayerListenerNormal(getServer(), ess, chatStore);
        final EssentialsChatPlayerListenerHighest playerListenerHighest = new EssentialsChatPlayerListenerHighest(getServer(), ess, chatStore);
        pluginManager.registerEvents(playerListenerLowest, this);
        pluginManager.registerEvents(playerListenerNormal, this);
        pluginManager.registerEvents(playerListenerHighest, this);
    }
}
