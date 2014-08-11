package com.earth2me.essentials.perm;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


public class ConfigPermissionsHandler extends SuperpermsHandler
{
	public ConfigPermissionsHandler(final Plugin ess)
	{
	}

	@Override
	public boolean hasPermission(final Player base, final String node)
	{
		return super.hasPermission(base, node);
	}

}
