package com.earth2me.essentials.signs;

import net.ess3.api.IEssentials;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;


public class SignEntityListener implements Listener
{
	private final transient IEssentials ess;

	public SignEntityListener(final IEssentials ess)
	{
		this.ess = ess;
	}

	@EventHandler(priority = EventPriority.LOW)
    @SuppressWarnings("unused")
	public void onSignEntityExplode(final EntityExplodeEvent event)
	{
		if (ess.getSettings().areSignsDisabled())
		{
			event.getHandlers().unregister(this);
			return;
		}
		
		for (Block block : event.blockList())
		{
			if (((block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST)
				 && EssentialsSign.isValidSign(new EssentialsSign.BlockSign(block))) | EssentialsSign.checkIfBlockBreaksSigns(block))
			{
				event.setCancelled(true);
				return;
			}
			for (EssentialsSign sign : ess.getSettings().enabledSigns())
			{
				if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType()))
				{
					event.setCancelled(!sign.onBlockExplode(block, ess));
					return;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    @SuppressWarnings("unused")
	public void onSignEntityChangeBlock(final EntityChangeBlockEvent event)
	{
		if (ess.getSettings().areSignsDisabled())
		{
			event.getHandlers().unregister(this);
			return;
		}

		final Block block = event.getBlock();
		if (((block.getType() == Material.WALL_SIGN
			  || block.getType() == Material.SIGN_POST)
			 && EssentialsSign.isValidSign(new EssentialsSign.BlockSign(block)))
			|| EssentialsSign.checkIfBlockBreaksSigns(block))
		{
			event.setCancelled(true);
			return;
		}
		for (EssentialsSign sign : ess.getSettings().enabledSigns())
		{
			if (sign.areHeavyEventRequired() && sign.getBlocks().contains(block.getType())
				&& !sign.onBlockBreak(block, ess))
			{
				event.setCancelled(true);
				return;
			}
		}
	}
}
