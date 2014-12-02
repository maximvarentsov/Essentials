package com.earth2me.essentials;

import com.earth2me.essentials.textreader.IText;
import com.earth2me.essentials.textreader.KeywordReplacer;
import com.earth2me.essentials.textreader.TextInput;
import com.earth2me.essentials.textreader.TextPager;
import com.earth2me.essentials.utils.LocationUtil;
import net.ess3.api.IEssentials;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.earth2me.essentials.I18n.tl;


public class EssentialsPlayerListener implements Listener
{
	private static final Logger LOGGER = Logger.getLogger("Essentials");
	private final transient IEssentials ess;

	public EssentialsPlayerListener(final IEssentials parent)
	{
		this.ess = parent;
	}

	@EventHandler(priority = EventPriority.NORMAL)
    @SuppressWarnings("unused")
	void onRespawn(final PlayerRespawnEvent event)
	{
		User user = ess.getUser(event.getPlayer());
		user.setDisplayNick();

		if (ess.getSettings().isTeleportInvulnerability())
		{
			user.enableInvulnerabilityAfterTeleport();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
	void onChat(final AsyncPlayerChatEvent event)
	{
		final User user = ess.getUser(event.getPlayer());
		if (user.isMuted())
		{
			event.setCancelled(true);
			user.sendMessage(tl("voiceSilenced"));
			LOGGER.info(tl("mutedUserSpeaks", user.getName()));
		}
		try
		{
			final Iterator<Player> it = event.getRecipients().iterator();
			while (it.hasNext())
			{
				final User u = ess.getUser(it.next());
				if (u.isIgnoredPlayer(user))
				{
					it.remove();
				}
			}
		}
		catch (UnsupportedOperationException ex)
		{
			if (ess.getSettings().isDebug())
			{
				ess.getLogger().log(Level.INFO, "Ignore could not block chat due to custom chat plugin event.", ex);
			}
			else
			{
				ess.getLogger().info("Ignore could not block chat due to custom chat plugin event.");
			}
		}

		user.setDisplayNick();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    void onPlayerQuit(final PlayerQuitEvent event)
	{
		final User user = ess.getUser(event.getPlayer());

		if (ess.getSettings().allowSilentJoinQuit())
		{
			event.setQuitMessage(null);
		}
		if (ess.getSettings().removeGodOnDisconnect() && user.isGodModeEnabled())
		{
			user.setGodModeEnabled(false);
		}
		if (user.isVanished())
		{
			user.setVanished(false);
		}
		user.setLogoutLocation();
		if (user.isRecipeSee())
		{
			user.getBase().getOpenInventory().getTopInventory().clear();
		}
		user.dispose();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
	void onJoin(final PlayerJoinEvent event)
	{
        if (ess.getSettings().allowSilentJoinQuit())
        {
            event.setJoinMessage(null);
        }
		ess.runTaskAsynchronously(() -> delayedJoin(event.getPlayer()));
	}

	void delayedJoin(final Player player)
	{
		if (!player.isOnline())
		{
			return;
		}

		final User dUser = ess.getUser(player);

		if (dUser.isNPC())
		{
			dUser.setNPC(false);
		}

		final long currentTime = System.currentTimeMillis();
		dUser.checkMuteTimeout(currentTime);

		IText tempInput = null;

		if (dUser.isAuthorized("essentials.motd"))
		{
			try
			{
				tempInput = new TextInput(dUser.getSource(), "motd", true, ess);
			}
			catch (IOException ex)
			{
				if (ess.getSettings().isDebug())
				{
					LOGGER.log(Level.WARNING, ex.getMessage(), ex);
				}
				else
				{
					LOGGER.log(Level.WARNING, ex.getMessage());
				}
			}
		}

		final IText input = tempInput;

		class DelayJoinTask implements Runnable
		{
			@Override
			public void run()
			{
				final User user = ess.getUser(player);

				if (!user.getBase().isOnline())
				{
					return;
				}

				user.startTransaction();

				user.setLastAccountName(user.getBase().getName());
				user.setLastLogin(currentTime);
				user.setDisplayNick();

				final String kitName = ess.getSettings().getNewPlayerKit();
				if (!kitName.isEmpty())
				{
					try
					{
						final Kit kit = new Kit(kitName.toLowerCase(Locale.ENGLISH), ess);
						kit.setTime(dUser);
						kit.expandItems(dUser);
					}
					catch (Exception ex)
					{
						LOGGER.log(Level.WARNING, ex.getMessage());
					}
				}

				if (!ess.getVanishedPlayers().isEmpty() && !user.isAuthorized("essentials.vanish.see"))
				{
					for (String p : ess.getVanishedPlayers())
					{
						Player toVanish = ess.getServer().getPlayerExact(p);
						if (toVanish != null && toVanish.isOnline())
						{
							user.getBase().hidePlayer(toVanish);
						}
					}
				}

				if (user.isAuthorized("essentials.sleepingignored"))
				{
					user.getBase().setSleepingIgnored(true);
				}

				if (ess.getSettings().allowSilentJoinQuit() && (user.isAuthorized("essentials.silentjoin") || user.isAuthorized("essentials.silentjoin.vanish"))) {
                    if (user.isAuthorized("essentials.silentjoin.vanish")) {
                        user.setVanished(true);
                    }
                }

				if (input != null && user.isAuthorized("essentials.motd"))
				{
					final IText output = new KeywordReplacer(input, user.getSource(), ess);
					final TextPager pager = new TextPager(output, true);
					pager.showPage("1", null, "motd", user.getSource());
				}

				if (user.isAuthorized("essentials.mail"))
				{
					List<String> mail = user.getMails();
					if (mail.isEmpty())
					{
						user.sendMessage(tl("noNewMail"));
					}
					else
					{
						user.sendMessage(tl("youHaveNewMail", mail.size()));
					}
				}

				if (user.isAuthorized("essentials.fly.safelogin"))
				{
					user.getBase().setFallDistance(0);
					if (LocationUtil.shouldFly(user.getLocation()))
					{
						user.getBase().setAllowFlight(true);
						user.getBase().setFlying(true);
						user.getBase().sendMessage(tl("flyMode", tl("enabled"), user.getDisplayName()));
					}
				}

				if (!user.isAuthorized("essentials.speed"))
				{
					user.getBase().setFlySpeed(0.1f);
					user.getBase().setWalkSpeed(0.2f);
				}

				user.stopTransaction();
			}
		}

		ess.scheduleSyncDelayedTask(new DelayJoinTask());
	}



	@EventHandler(priority = EventPriority.HIGH)
    @SuppressWarnings("unused")
    void onPlayerLogin(final PlayerLoginEvent event) {
        if (!event.getResult().equals(PlayerLoginEvent.Result.KICK_FULL)) {
            return;
        }
        if (ess.getUser(event.getPlayer()).isAuthorized("essentials.joinfullserver")) {
            event.allow();
        }
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    @SuppressWarnings("unused")
    void onPlayerTeleport(final PlayerTeleportEvent event) {
        User user = ess.getUser(event.getPlayer());

        boolean backListener = ess.getSettings().registerBackInListener();
        boolean teleportInvulnerability = ess.getSettings().isTeleportInvulnerability();

        if (event.getCause() == TeleportCause.PLUGIN || event.getCause() == TeleportCause.COMMAND) {
            if (backListener) {
                user.setLastLocation();
            }
            if (teleportInvulnerability) {
                user.enableInvulnerabilityAfterTeleport();
            }
        }
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    @SuppressWarnings("unused")
    void onCommandPreprocess(final PlayerCommandPreprocessEvent event)
	{
		final Player player = event.getPlayer();
		final String cmd = event.getMessage().toLowerCase(Locale.ENGLISH).split(" ")[0].replace("/", "").toLowerCase(Locale.ENGLISH);
		if (ess.getSettings().getSocialSpyCommands().contains(cmd) || ess.getSettings().getSocialSpyCommands().contains("*"))
		{
			for (User spyer : ess.getOnlineUsers())
			{
				if (spyer.isSocialSpyEnabled() && !player.equals(spyer.getBase()))
				{
					spyer.sendMessage(player.getDisplayName() + " : " + event.getMessage());
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
    @SuppressWarnings("unused")
    void onChangedWorldFlyReset(final PlayerChangedWorldEvent event)
	{
		User user = ess.getUser(event.getPlayer());

		if ((user.getBase().getGameMode() != GameMode.CREATIVE && user.getBase().getGameMode() != GameMode.SPECTATOR) && !user.isAuthorized("essentials.fly"))
		{
			user.getBase().setFallDistance(0f);
			user.getBase().setAllowFlight(false);
		}
		if (!user.isAuthorized("essentials.speed"))
		{
			user.getBase().setFlySpeed(0.1f);
			user.getBase().setWalkSpeed(0.2f);
		}
		else
		{
			if (user.getBase().getFlySpeed() > ess.getSettings().getMaxFlySpeed() && !user.isAuthorized("essentials.speed.bypass"))
			{
				user.getBase().setFlySpeed((float)ess.getSettings().getMaxFlySpeed());
			}
			else
			{
				user.getBase().setFlySpeed(user.getBase().getFlySpeed() * 0.99999f);
			}

			if (user.getBase().getWalkSpeed() > ess.getSettings().getMaxWalkSpeed() && !user.isAuthorized("essentials.speed.bypass"))
			{
				user.getBase().setWalkSpeed((float)ess.getSettings().getMaxWalkSpeed());
			}
			else
			{
				user.getBase().setWalkSpeed(user.getBase().getWalkSpeed() * 0.99999f);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
    @SuppressWarnings("unused")
    void onChangedWorld(final PlayerChangedWorldEvent event)
	{
		final User user = ess.getUser(event.getPlayer());
		final String newWorld = event.getPlayer().getLocation().getWorld().getName();
		user.setDisplayNick();
		if (ess.getSettings().getNoGodWorlds().contains(newWorld) && user.isGodModeEnabledRaw())
		{
			user.sendMessage(tl("noGodWorldWarning"));
		}

		if (!user.getWorld().getName().equals(newWorld))
		{
			user.sendMessage(tl("currentWorld", newWorld));
		}
		if (user.isVanished())
		{
			user.setVanished(user.isAuthorized("essentials.vanish"));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
    @SuppressWarnings("unused")
    void onInteract(final PlayerInteractEvent event)
	{
		switch (event.getAction())
		{
            case RIGHT_CLICK_BLOCK:
                if (!event.isCancelled() && event.getClickedBlock().getType() == Material.BED_BLOCK && ess.getSettings().getUpdateBedAtDaytime())
                {
                    User player = ess.getUser(event.getPlayer());
                    if (player.isAuthorized("essentials.sethome.bed"))
                    {
                        player.getBase().setBedSpawnLocation(event.getClickedBlock().getLocation());
                        player.sendMessage(tl("bedSet", player.getLocation().getWorld().getName(), player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()));
                    }
                }
                break;
            case LEFT_CLICK_BLOCK:
                if (event.getItem() != null && event.getItem().getType() != Material.AIR)
                {
                    final User user = ess.getUser(event.getPlayer());
                    if (user.hasPowerTools() && user.arePowerToolsEnabled() && usePowertools(user, event.getItem().getTypeId()))
                    {
                        event.setCancelled(true);
                    }
                }
                break;
		}
	}

	boolean usePowertools(final User user, final int id)
	{
		final List<String> commandList = user.getPowertool(id);
		if (commandList == null || commandList.isEmpty())
		{
			return false;
		}
		boolean used = false;
		// We need to loop through each command and execute
		for (final String command : commandList)
		{
			if (command.contains("{player}"))
			{
            }
			else if (command.startsWith("c:"))
			{
				used = true;
				user.getBase().chat(command.substring(2));
			}
			else
			{
				used = true;

				class PowerToolUseTask implements Runnable
				{
					@Override
					public void run()
					{
						user.getServer().dispatchCommand(user.getBase(), command);
						LOGGER.log(Level.INFO, String.format("[PT] %s issued server command: /%s", user.getName(), command));
					}
				}
				ess.scheduleSyncDelayedTask(new PowerToolUseTask());

			}
		}
		return used;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    @SuppressWarnings("unused")
    void onInventoryClick(final InventoryClickEvent event)
	{
		Player refreshPlayer = null;
		final Inventory top = event.getView().getTopInventory();
		final InventoryType type = top.getType();

		if (type == InventoryType.PLAYER)
		{
			final User user = ess.getUser((Player)event.getWhoClicked());
			final InventoryHolder invHolder = top.getHolder();
			if (invHolder != null && invHolder instanceof HumanEntity)
			{
				final User invOwner = ess.getUser((Player)invHolder);
				if (user.isInvSee() && (!user.isAuthorized("essentials.invsee.modify")
										|| invOwner.isAuthorized("essentials.invsee.preventmodify")
										|| !invOwner.getBase().isOnline()))
				{
					event.setCancelled(true);
					refreshPlayer = user.getBase();
				}
			}
		}
		else if (type == InventoryType.ENDER_CHEST)
		{
			final User user = ess.getUser((Player)event.getWhoClicked());
			if (user.isEnderSee() && (!user.isAuthorized("essentials.enderchest.modify")))
			{
				event.setCancelled(true);
				refreshPlayer = user.getBase();
			}
		}
		else if (type == InventoryType.WORKBENCH)
		{
			User user = ess.getUser((Player)event.getWhoClicked());
			if (user.isRecipeSee())
			{
				event.setCancelled(true);
				refreshPlayer = user.getBase();
			}
		}
		else if (type == InventoryType.CHEST && top.getSize() == 9)
		{
			final User user = ess.getUser((Player)event.getWhoClicked());
			final InventoryHolder invHolder = top.getHolder();
			if (invHolder != null && invHolder instanceof HumanEntity && user.isInvSee())
			{
				event.setCancelled(true);
				refreshPlayer = user.getBase();
			}
		}

		if (refreshPlayer != null)
		{
            ess.scheduleSyncDelayedTask(refreshPlayer::updateInventory, 1);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
    @SuppressWarnings("unused")
    void onInventoryClose(final InventoryCloseEvent event) {
		Player refreshPlayer = null;
		final Inventory top = event.getView().getTopInventory();
		final InventoryType type = top.getType();
		if (type == InventoryType.PLAYER) {
			final User user = ess.getUser((Player)event.getPlayer());
			user.setInvSee(false);
			refreshPlayer = user.getBase();
		} else if (type == InventoryType.ENDER_CHEST) {
			final User user = ess.getUser((Player)event.getPlayer());
			user.setEnderSee(false);
			refreshPlayer = user.getBase();
		} else if (type == InventoryType.WORKBENCH) {
			final User user = ess.getUser((Player)event.getPlayer());
			if (user.isRecipeSee())
			{
				user.setRecipeSee(false);
				event.getView().getTopInventory().clear();
				refreshPlayer = user.getBase();
			}
		} else if (type == InventoryType.CHEST && top.getSize() == 9) {
			final InventoryHolder invHolder = top.getHolder();
			if (invHolder != null && invHolder instanceof HumanEntity)
			{
				final User user = ess.getUser((Player)event.getPlayer());
				user.setInvSee(false);
				refreshPlayer = user.getBase();
			}
		}

		if (refreshPlayer != null) {
            refreshPlayer.updateInventory();
		}
	}
}
