/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cubeengine.service.command;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import de.cubeisland.engine.butler.CommandBase;
import de.cubeisland.engine.butler.CommandDescriptor;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.Dispatcher;
import de.cubeisland.engine.butler.DispatcherCommand;
import de.cubeisland.engine.butler.SimpleCommandDescriptor;
import de.cubeisland.engine.butler.alias.AliasCommand;
import de.cubeisland.engine.butler.parametric.ParametricContainerCommand;
import org.cubeengine.service.i18n.I18n;
import org.cubeengine.service.i18n.formatter.MessageType;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.util.command.CommandSource;

import static org.cubeengine.service.i18n.formatter.MessageType.NEUTRAL;
import static org.spongepowered.api.text.format.TextColors.*;

public class HelpCommand implements CommandBase
{
    private static final SimpleCommandDescriptor helpDescriptor = new SimpleCommandDescriptor();
    static
    {
        helpDescriptor.setName("?");
        helpDescriptor.setDescription("Displays Help");
    }
    private Dispatcher helpTarget;
    private I18n i18n;

    public HelpCommand(Dispatcher target, I18n i18n)
    {
        this.helpTarget = target;
        this.i18n = i18n;
    }

    @Override
    public boolean execute(CommandInvocation invocation)
    {
        if (!(invocation.getCommandSource() instanceof CommandSource))
        {
            return false;
        }
        CommandDescriptor descriptor = helpTarget.getDescriptor();
        CommandSource sender = (CommandSource)invocation.getCommandSource();

        TextFormat formatGray = new TextFormat(GRAY);
        i18n.sendTranslated(sender, formatGray, "Description: {input}", Texts.toPlain(i18n.getTranslation(sender, MessageType.NONE, descriptor.getDescription())));

        List<String> labels = new ArrayList<>(invocation.getLabels());
        if (labels.isEmpty())
        {
            labels.add("");
        }
        if ("?".equals(labels.get(labels.size() - 1)))
        {
            labels.remove(labels.size() - 1);
        }

        i18n.sendTranslated(sender, formatGray, "Usage: {input}", descriptor.getUsage(invocation, labels.toArray(new String[labels.size()])));
        sender.sendMessage(Texts.of());

        if (helpTarget instanceof DispatcherCommand)
        {
            Set<CommandBase> commands = helpTarget.getCommands();
            if (!commands.isEmpty() && (commands.size() != 1
                || !(commands.iterator().next() instanceof HelpCommand))) // is Empty ignoring HelpCommand
            {
                i18n.sendTranslated(sender, NEUTRAL, "The following sub-commands are available:");
                sender.sendMessage(Texts.of());
                for (CommandBase command : commands)
                {
                    if (command instanceof HelpCommand
                     || command instanceof AliasCommand && commands.contains(((AliasCommand)command).getTarget()))
                    {
                        continue;
                    }
                    sender.sendMessage(Texts.of(YELLOW, command.getDescriptor().getName(), WHITE, ": ",
                        i18n.getTranslation(sender, formatGray, command.getDescriptor().getDescription())));
            }
                sender.sendMessage(Texts.of());
            }
            else if (helpTarget instanceof ParametricContainerCommand)
            {
                i18n.sendTranslated(sender, MessageType.NEGATIVE, "No actions are available");
                sender.sendMessage(Texts.of());
            }
        }

        // TODO currently we have nothing here:
        /*
        if (descriptor instanceof CubeDescriptor)
        {
            sender.sendTranslated(GRAY, "Detailed help: {input#link:color=INDIGO}", "http://engine.cubeisland.de/c/" + ((CubeDescriptor)descriptor).getModule().getInformation().getName().toLowerCase() + "/" + StringUtils.implode("/", labels));
        }
        */
        return true;
    }

    @Override
    public CommandDescriptor getDescriptor()
    {
        return helpDescriptor;
    }

    @Override
    public List<String> getSuggestions(CommandInvocation call)
    {
        return null; // No Suggestions
    }

    /*
    TODO ConversationHelp
    context.sendTranslated(NEUTRAL, "Flags:");
            Set<String> flags = new HashSet<>();
            for (FlagParameter flag : helpTarget.getContextFactory().descriptor().getFlags())
            {
                flags.add(flag.getLongName().toLowerCase());
            }
            context.sendMessage("    " + StringUtils.implode(ChatFormat.GREY + ", " + ChatFormat.WHITE, flags));
            context.sendTranslated(NEUTRAL, "Parameters:");
            Set<String> params  = new HashSet<>();
            for (NamedParameter param : helpTarget.getContextFactory().descriptor().getNamedGroups().listAll())
            {
                params.add(param.getName().toLowerCase());
            }
            context.sendMessage("    " + StringUtils.implode(ChatFormat.GREY + ", " + ChatFormat.WHITE, params));
            return null;
     */
}
