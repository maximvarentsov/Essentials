package com.earth2me.essentials.perm;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import ru.gtncraft.permissions.Permissions;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PermissionsHandler implements IPermissionsHandler
{
	private transient IPermissionsHandler handler = new NullPermissionsHandler();
	private transient String defaultGroup = "default";
	private final transient Plugin plugin;
	private static final Logger LOGGER = Logger.getLogger("Essentials");
	private transient boolean useSuperperms = false;

	public PermissionsHandler(final Plugin plugin)
	{
		this.plugin = plugin;
	}

	public PermissionsHandler(final Plugin plugin, final boolean useSuperperms)
	{
		this.plugin = plugin;
		this.useSuperperms = useSuperperms;
	}

	public PermissionsHandler(final Plugin plugin, final String defaultGroup)
	{
		this.plugin = plugin;
		this.defaultGroup = defaultGroup;
	}

	@Override
	public String getGroup(final Player base)
	{
		String group = handler.getGroup(base);
		if (group == null)
		{
			group = defaultGroup;
		}
		return group;
	}

	@Override
	public List<String> getGroups(final Player base)
	{
		List<String> groups = handler.getGroups(base);
		if (groups == null || groups.isEmpty())
		{
			groups = Collections.singletonList(defaultGroup);
		}
		return Collections.unmodifiableList(groups);
	}

	@Override
	public boolean canBuild(final Player base, final String group)
	{
		return handler.canBuild(base, group);
	}

	@Override
	public boolean inGroup(final Player base, final String group)
	{
		return handler.inGroup(base, group);
	}

	@Override
	public boolean hasPermission(final Player base, final String node)
	{
		return handler.hasPermission(base, node);
	}

	@Override
	public String getPrefix(final Player base)
	{
		String prefix = handler.getPrefix(base);
		if (prefix == null)
		{
			prefix = "";
		}
		return prefix;
	}

	@Override
	public String getSuffix(final Player base)
	{
		String suffix = handler.getSuffix(base);
		if (suffix == null)
		{
			suffix = "";
		}
		return suffix;
	}

	public void checkPermissions()
	{
		final PluginManager pluginManager = plugin.getServer().getPluginManager();

		final Plugin permBukkitPlugin = pluginManager.getPlugin("Permissions");
		if (permBukkitPlugin != null && permBukkitPlugin.isEnabled())
		{
			if (!(handler instanceof Permissions))
			{
				LOGGER.log(Level.INFO, "Essentials: Using Permissions based permissions.");
				handler = new GPermissionsHandler(permBukkitPlugin);
			}
			return;
		}

		if (useSuperperms)
		{
			if (!(handler instanceof SuperpermsHandler))
			{
				LOGGER.log(Level.INFO, "Essentials: Using superperms based permissions.");
				handler = new SuperpermsHandler();
			}
		}
		else
		{
			if (!(handler instanceof ConfigPermissionsHandler))
			{
				LOGGER.log(Level.INFO, "Essentials: Using config file enhanced permissions.");
				LOGGER.log(Level.INFO, "Permissions listed in as player-commands will be given to all users.");
				handler = new ConfigPermissionsHandler(plugin);
			}
		}
	}

	public void setUseSuperperms(final boolean useSuperperms)
	{
		this.useSuperperms = useSuperperms;
	}

	public String getName()
	{
		return handler.getClass().getSimpleName().replace("Handler", "");
	}
}
