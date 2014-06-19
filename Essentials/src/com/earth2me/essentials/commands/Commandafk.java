package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import org.bukkit.Server;

import static com.earth2me.essentials.I18n.tl;


public class Commandafk extends EssentialsCommand
{
	public Commandafk()
	{
		super("afk");
	}

	@Override
	public void run(Server server, User user, String commandLabel, String[] args) throws Exception
	{
		if (args.length > 0 && user.isAuthorized("essentials.afk.others"))
		{
			User afkUser = getPlayer(server, user, args, 0);
			toggleAfk(afkUser);
		}
		else
		{
			toggleAfk(user);
		}
	}
	
	@Override
	public void run(Server server, CommandSource sender, String commandLabel, String[] args) throws Exception
	{
		if (args.length > 0)
		{
			User afkUser = getPlayer(server, args, 0, true, false);
			toggleAfk(afkUser);
		}
		else
		{
			throw new NotEnoughArgumentsException();
		}
	}

	private void toggleAfk(User user)
	{
		user.setDisplayNick();
		if (!user.toggleAfk())
		{
			user.updateActivity(false);
            user.sendMessage(tl("userIsNotAway", user.getDisplayName()));
		}
		else
        {
            user.sendMessage(tl("userIsAway", user.getDisplayName()));
        }
	}
}

