package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.World;

import static com.earth2me.essentials.I18n.tl;

@SuppressWarnings("unused")
public class Commandweather extends EssentialsCommand
{
	public Commandweather()
	{
		super("weather");
	}

	//TODO: Remove duplication
	@Override
	public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception
	{
		final boolean isStorm;
		if (args.length < 1)
		{
			if (commandLabel.equalsIgnoreCase("sun"))
			{
				isStorm = false;
			}
			else if (commandLabel.equalsIgnoreCase("storm") || commandLabel.equalsIgnoreCase("rain"))
			{
				isStorm = true;
			}
			else
			{
				throw new NotEnoughArgumentsException();
			}
		}
		else
		{
			isStorm = args[0].equalsIgnoreCase("storm");
		}
		final World world = user.getWorld();
		if (args.length > 1)
		{

			world.setStorm(isStorm);
			world.setWeatherDuration(Integer.parseInt(args[1]) * 20);
			user.sendMessage(isStorm
							 ? tl("weatherStormFor", world.getName(), args[1])
							 : tl("weatherSunFor", world.getName(), args[1]));
		}
		else
		{
			world.setStorm(isStorm);
			user.sendMessage(isStorm
							 ? tl("weatherStorm", world.getName())
							 : tl("weatherSun", world.getName()));
		}
	}

	@Override
	protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception
	{
		if (args.length < 2) //running from console means inserting a world arg before other args
		{
			throw new Exception("When running from console, usage is: /" + commandLabel + " <world> <storm/sun> [duration]");
		}

		final boolean isStorm = args[1].equalsIgnoreCase("storm");
		final World world = server.getWorld(args[0]);
		if (world == null)
		{
			throw new Exception(tl("weatherInvalidWorld", args[0]));
		}
		if (args.length > 2)
		{

			world.setStorm(isStorm);
			world.setWeatherDuration(Integer.parseInt(args[2]) * 20);
			sender.sendMessage(isStorm
							   ? tl("weatherStormFor", world.getName(), args[2])
							   : tl("weatherSunFor", world.getName(), args[2]));
		}
		else
		{
			world.setStorm(isStorm);
			sender.sendMessage(isStorm
							   ? tl("weatherStorm", world.getName())
							   : tl("weatherSun", world.getName()));
		}
	}
}
