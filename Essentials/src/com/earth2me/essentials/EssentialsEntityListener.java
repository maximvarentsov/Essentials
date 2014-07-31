package com.earth2me.essentials;

import net.ess3.api.IEssentials;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

import static com.earth2me.essentials.I18n.tl;


public class EssentialsEntityListener implements Listener
{
	private final IEssentials ess;

	public EssentialsEntityListener(IEssentials ess)
	{
		this.ess = ess;
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    @SuppressWarnings("unused")
	void onEntityDamage(final EntityDamageEvent event)
	{
		if (event.getEntity() instanceof Player && ess.getUser((Player)event.getEntity()).isGodModeEnabled())
		{
			final Player player = (Player)event.getEntity();
			player.setFireTicks(0);
			player.setRemainingAir(player.getMaximumAir());
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    @SuppressWarnings("unused")
	void onEntityCombust(final EntityCombustEvent event)
	{
		if (event.getEntity() instanceof Player && ess.getUser((Player)event.getEntity()).isGodModeEnabled())
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
	void onPlayerDeathEvent(final PlayerDeathEvent event)
	{
		final User user = ess.getUser(event.getEntity());
		if (user.isAuthorized("essentials.back.ondeath") && !ess.getSettings().isCommandDisabled("back"))
		{
			user.setLastLocation();
			user.sendMessage(tl("backAfterDeath"));
		}
		if (!ess.getSettings().areDeathMessagesEnabled())
		{
			event.setDeathMessage("");
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    @SuppressWarnings("unused")
	void onFoodLevelChange(final FoodLevelChangeEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			final User user = ess.getUser( (Player) event.getEntity());
			if (user.isGodModeEnabled())
			{
				if (user.isGodModeEnabledRaw())
				{
					user.getBase().setFoodLevel(20);
					user.getBase().setSaturation(10);
				}
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    @SuppressWarnings("unused")
	void onEntityRegainHealth(final EntityRegainHealthEvent event)
	{
		if (event.getRegainReason() == RegainReason.SATIATED && event.getEntity() instanceof Player
			&& ess.getUser((Player)event.getEntity()).isAfk())
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    @SuppressWarnings("unused")
	void onPotionSplash(final PotionSplashEvent event)
	{
        event.getAffectedEntities()
             .stream()
             .filter(entity -> entity instanceof Player && ess.getUser((Player) entity)
             .isGodModeEnabled())
             .forEach(entity -> event.setIntensity(entity, 0d));
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    @SuppressWarnings("unused")
	void onEntityShootBow(final EntityShootBowEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			User user = ess.getUser((Player) event.getEntity());
			if (user.isAfk())
			{
				user.updateActivity();
			}
		}
	}
}
