package com.earth2me.essentials.perm;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.gtncraft.permissions.Group;
import ru.gtncraft.permissions.PermissionInfo;
import ru.gtncraft.permissions.Permissions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class GPermissionsHandler extends SuperpermsHandler
{
	private final transient Permissions plugin;

	public GPermissionsHandler(final Plugin plugin)
	{
		this.plugin = (Permissions)plugin;
	}

	@Override
	public String getGroup(final Player base)
	{
		final Collection<Group> groups = getPBGroups(base);
		if (groups == null || groups.isEmpty())
		{
			return null;
		}
		return ((List<Group>) groups).get(0).getName();
	}

	@Override
	public List<String> getGroups(final Player base)
	{
		final Collection<Group> groups = getPBGroups(base);
		if (groups.size() == 1)
		{
			return Collections.singletonList(((List<Group>)groups).get(0).getName());
		}
		final List<String> groupNames = new ArrayList<>(groups.size());
		for (Group group : groups)
		{
			groupNames.add(group.getName());
		}
		return groupNames;
	}

	private Collection<Group> getPBGroups(final Player base)
	{
		final PermissionInfo info = plugin.getManager().getPlayerInfo(base.getName());
		if (info == null)
		{
			return Collections.emptyList();
		}
		final Collection<Group> groups = info.getGroups();
		if (groups == null || groups.isEmpty())
		{
			return Collections.emptyList();
		}
		return groups;
	}

	@Override
	public boolean inGroup(final Player base, final String group)
	{
		final Collection<Group> groups = getPBGroups(base);
		for (Group group1 : groups)
		{
			if (group1.getName().equalsIgnoreCase(group))
			{
				return true;
			}
		}
		return false;
	}

}
