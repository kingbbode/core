package de.cubeisland.engine.module.vanillaplus;

import java.util.Set;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.filter.Restricted;
import de.cubeisland.engine.butler.parametric.Command;
import de.cubeisland.engine.module.core.command.CommandSender;
import de.cubeisland.engine.module.core.command.ContainerCommand;
import de.cubeisland.engine.module.core.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.module.core.sponge.SpongeCore;
import de.cubeisland.engine.module.core.user.User;
import org.spongepowered.api.data.manipulators.entities.WhitelistData;

import static de.cubeisland.engine.module.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.module.core.util.formatter.MessageType.NEUTRAL;
import static de.cubeisland.engine.module.core.util.formatter.MessageType.POSITIVE;

@Command(name = "whitelist", desc = "Allows you to manage your whitelist")
public class WhitelistCommand extends ContainerCommand
{
    private final SpongeCore core;

    public WhitelistCommand(SpongeCore core)
    {
        super(core);
        this.core = core;
    }

    @Override
    protected boolean selfExecute(CommandInvocation invocation)
    {
        if (invocation.isConsumed())
        {
            return this.getCommand("list").execute(invocation);
        }
        else if (invocation.tokens().size() - invocation.consumed() == 1)
        {
            return this.getCommand("add").execute(invocation);
        }
        return super.execute(invocation);
    }

    @Command(desc = "Adds a player to the whitelist.")
    public void add(CommandSender context, User player)
    {
        if (player.getData(WhitelistData.class).isPresent())
        {
            context.sendTranslated(NEUTRAL, "{user} is already whitelisted.", player);
            return;
        }
        player.offer(core.getGame().getRegistry().getBuilderOf(WhitelistData.class).get());
        context.sendTranslated(POSITIVE, "{user} is now whitelisted.", player);
    }

    @Command(alias = "rm", desc = "Removes a player from the whitelist.")
    public void remove(CommandSender context, User player)
    {
        if (!player.getData(WhitelistData.class).isPresent())
        {
            context.sendTranslated(NEUTRAL, "{user} is not whitelisted.", player);
            return;
        }
        player.getOfflinePlayer().remove(WhitelistData.class);
        context.sendTranslated(POSITIVE, "{user} is not whitelisted anymore.", player.getName());
    }

    @Command(desc = "Lists all the whitelisted players")
    public void list(CommandSender context)
    {
        Set<org.spongepowered.api.entity.player.User> whitelist = this.core.getGame().getServer().getWhitelistedPlayers();
        if (!this.core.getGame().getServer().hasWhitelist())
        {
            context.sendTranslated(NEUTRAL, "The whitelist is currently disabled.");
        }
        else
        {
            context.sendTranslated(POSITIVE, "The whitelist is enabled!.");
        }
        context.sendMessage(" ");
        if (whitelist.isEmpty())
        {
            context.sendTranslated(NEUTRAL, "There are currently no whitelisted players!");
        }
        else
        {
            context.sendTranslated(NEUTRAL, "The following players are whitelisted:");
            for (org.spongepowered.api.entity.player.User player : whitelist)
            {
                context.sendMessage(" - " + player.getName());
            }
        }
        Set<org.spongepowered.api.entity.player.User> operators = this.core.getGame().getServer().getOperators();
        if (!operators.isEmpty())
        {
            context.sendTranslated(NEUTRAL, "The following players are OP and can bypass the whitelist");
            for (org.spongepowered.api.entity.player.User operator : operators)
            {
                context.sendMessage(" - " + operator.getName());
            }
        }
    }

    @Command(desc = "Enables the whitelisting")
    public void on(CommandSender context)
    {
        if (this.core.getGame().getServer().hasWhitelist())
        {
            context.sendTranslated(NEGATIVE, "The whitelist is already enabled!");
            return;
        }
        this.core.getGame().getServer().setHasWhitelist(true);
        context.sendTranslated(POSITIVE, "The whitelist is now enabled.");
    }

    @Command(desc = "Disables the whitelisting")
    public void off(CommandSender context)
    {
        if (!this.core.getGame().getServer().hasWhitelist())
        {
            context.sendTranslated(NEGATIVE, "The whitelist is already disabled!");
            return;
        }
        this.core.getGame().getServer().setHasWhitelist(false);
        context.sendTranslated(POSITIVE, "The whitelist is now disabled.");
    }

    @Command(desc = "Wipes the whitelist completely")
    @Restricted(value = ConsoleCommandSender.class, msg = "This command is too dangerous for users!")
    public void wipe(CommandSender context)
    {
        // TODO wipe whitelist
        context.sendTranslated(POSITIVE, "The whitelist was successfully wiped!");
    }
}