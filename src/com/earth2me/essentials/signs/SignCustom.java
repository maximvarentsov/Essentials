package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.User;
import net.ess3.api.IEssentials;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;

import static com.earth2me.essentials.I18n.tl;

public class SignCustom extends EssentialsSign {

    public SignCustom() {
        super("Custom");
    }

    @Override
    protected boolean onSignCreate(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException
    {
        final String command = sign.getLine(1);
        final String commandRoot;

        if (command.startsWith("/")) {
            commandRoot = command.substring(1).split(" ")[0];
        } else {
            commandRoot = command.split(" ")[0];
            sign.setLine(1, "/" + command);
        }

        if (command.isEmpty())
        {
            sign.setLine(1, "§c/<command> [args]");
            throw new SignException(tl("invalidSignLine", 1));
        }
        else
        {
            try
            {
                PluginCommand pluginCommand = Bukkit.getPluginCommand(commandRoot);
                if (pluginCommand == null)
                {
                    throw new SignException(String.format("Command /%s not found", command));
                }
            }
            catch (Exception ex)
            {
                throw new SignException(ex.getMessage(), ex);
            }
            return true;
        }
    }

    @Override
    protected boolean onSignInteract(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException
    {
        final String command = sign.getLine(1);
        try
        {
            player.getBase().performCommand(command.substring(1));
        }
        catch (Exception ex)
        {
            throw new SignException(ex.getMessage(), ex);
        }
        return true;
    }
}
