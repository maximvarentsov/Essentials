package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import org.bukkit.Server;

@SuppressWarnings("unused")
@Deprecated
public class Commandtppos extends EssentialsCommand
{
	public Commandtppos()
	{
		super("tppos");
	}

	@Override
	public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception
	{
        user.sendMessage(commandLabel + " deprecated use /tp x y z");
    }

	@Override
	public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception
	{
		sender.sendMessage(commandLabel + " deprecated use /tp x y z");
	}
}
