package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import org.bukkit.Server;

import static com.earth2me.essentials.I18n.tl;

@SuppressWarnings("unused")
public class Commandtpaall extends EssentialsCommand
{
	public Commandtpaall()
	{
		super("tpaall");
	}

	@Override
	public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception
	{
		if (args.length < 1)
		{
			if (sender.isPlayer())
			{
				teleportAAllPlayers(server, sender, ess.getUser(sender.getPlayer()));
				return;
			}
			throw new NotEnoughArgumentsException();
		}

		final User target = getPlayer(server, sender, args, 0);
		teleportAAllPlayers(server, sender, target);
	}

	private void teleportAAllPlayers(final Server server, final CommandSource sender, final User target)
	{
		sender.sendMessage(tl("teleportAAll"));
		for (User player : ess.getOnlineUsers())
		{
			if (target == player)
			{
				continue;
			}
			if (!player.isTeleportEnabled())
			{
				continue;
			}
			if (sender.equals(target.getBase())
				&& target.getWorld() != player.getWorld() && ess.getSettings().isWorldTeleportPermissions()
				&& !target.isAuthorized("essentials.worlds." + target.getWorld().getName()))
			{
				continue;
			}
			try
			{
				player.requestTeleport(target, true);
				player.sendMessage(tl("teleportHereRequest", target.getDisplayName()));
				player.sendMessage(tl("typeTpaccept"));
				if (ess.getSettings().getTpaAcceptCancellation() != 0)
				{
					player.sendMessage(tl("teleportRequestTimeoutInfo", ess.getSettings().getTpaAcceptCancellation()));
				}
			}
			catch (Exception ex)
			{
				ess.showError(sender, ex, getName());
			}
		}
	}
}
