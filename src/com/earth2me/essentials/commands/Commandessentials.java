package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.UserMap;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.NumberUtil;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.earth2me.essentials.I18n.tl;

@SuppressWarnings("unused")
public class Commandessentials extends EssentialsCommand
{
	public Commandessentials()
	{
		super("essentials");
	}
	private transient int taskid;
	private final transient Map<Player, Block> noteBlocks = new HashMap<>();

	@Override
	public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception
	{
		if (args.length == 0)
		{
			run_disabled(server, sender, commandLabel, args);
		}
		else if (args[0].equalsIgnoreCase("debug"))
		{
			run_debug(server, sender, commandLabel, args);
		}
		else if (args[0].equalsIgnoreCase("reset"))
		{
			run_reset(server, sender, commandLabel, args);
		}
		else if (args[0].equalsIgnoreCase("cleanup"))
		{
			run_cleanup(server, sender, commandLabel, args);
		}
		else
		{
			run_reload(server, sender, commandLabel, args);
		}
	}

	//If you do not supply an argument this command will list 'overridden' commands.
	private void run_disabled(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception
	{
		sender.sendMessage("/<command> <reload/debug>");

		final StringBuilder disabledCommands = new StringBuilder();
		for (Map.Entry<String, String> entry : ess.getAlternativeCommandsHandler().disabledCommands().entrySet())
		{
			if (disabledCommands.length() > 0)
			{
				disabledCommands.append(", ");
			}
			disabledCommands.append(entry.getKey()).append(" => ").append(entry.getValue());
		}
		if (disabledCommands.length() > 0)
		{
			sender.sendMessage(tl("blockList"));
			sender.sendMessage(disabledCommands.toString());
		}
	}

	private void run_reset(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception
	{
		if (args.length < 2)
		{
			throw new Exception("/<command> reset <player>");
		}
		final User user = getPlayer(server, args, 1, true, true);
		user.reset();
		sender.sendMessage("Reset Essentials userdata for player: " + user.getDisplayName());
	}

	private void run_debug(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception
	{
		ess.getSettings().setDebug(!ess.getSettings().isDebug());
		sender.sendMessage("Essentials " + ess.getDescription().getVersion() + " debug mode " + (ess.getSettings().isDebug() ? "enabled" : "disabled"));
	}

	private void run_reload(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception
	{
		ess.reload();
		sender.sendMessage(tl("essentialsReload", ess.getDescription().getVersion()));
	}

	private void run_cleanup(final Server server, final CommandSource sender, final String command, final String args[]) throws Exception
	{
		if (args.length < 2 || !NumberUtil.isInt(args[1]))
		{
			sender.sendMessage("This sub-command will delete users who havent logged in in the last <days> days.");
			sender.sendMessage("Optional parameters define the minium amount required to prevent deletion.");
			sender.sendMessage("Unless you define larger default values, this command wil ignore people who have more than 0 money/homes.");
			throw new Exception("/<command> cleanup <days> [money] [homes]");
		}
		sender.sendMessage(tl("cleaning"));

		final long daysArg = Long.parseLong(args[1]);
		final double moneyArg = args.length >= 3 ? Double.parseDouble(args[2].replaceAll("[^0-9\\.]", "")) : 0;
		final int homesArg = args.length >= 4 && NumberUtil.isInt(args[3]) ? Integer.parseInt(args[3]) : 0;
		final UserMap userMap = ess.getUserMap();

		ess.runTaskAsynchronously(() -> {
            Long currTime = System.currentTimeMillis();
            for (UUID u : userMap.getAllUniqueUsers())
            {
                final User user = ess.getUserMap().getUser(u);
                if (user == null)
                {
                    continue;
                }

                long lastLog = user.getLastLogout();
                if (lastLog == 0)
                {
                    lastLog = user.getLastLogin();
                }
                if (lastLog == 0)
                {
                    user.setLastLogin(currTime);
                }

                if (user.isNPC())
                {
                    continue;
                }

                long timeDiff = currTime - lastLog;
                long milliDays = daysArg * 24L * 60L * 60L * 1000L;
                int homeCount = user.getHomes().size();
                double moneyCount = user.getMoney().doubleValue();

                if ((lastLog == 0) || (timeDiff < milliDays)
                    || (homeCount > homesArg) || (moneyCount > moneyArg))
                {
                    continue;
                }

                if (ess.getSettings().isDebug())
                {
                    ess.getLogger().info("Deleting user: " + user.getName() + " Money: " + moneyCount + " Homes: " + homeCount + " Last seen: " + DateUtil.formatDateDiff(lastLog));
                }

                user.reset();
            }
            sender.sendMessage(tl("cleaned"));
        });

	}
}
