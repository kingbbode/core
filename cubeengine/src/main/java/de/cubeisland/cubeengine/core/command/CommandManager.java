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
package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.command.result.confirm.ConfirmResult;
import de.cubeisland.cubeengine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.Cleanable;

/**
 * This class manages the registration of commands.
 */
public interface CommandManager extends Cleanable
{
    void registerCommand(CubeCommand command, String... parents);
    void registerCommands(Module module, CommandHolder commandHolder, String... parents);
    void registerCommands(Module module, Object commandHolder, Class<? extends CubeCommand> commandType, String... parents);
    void registerCommandFactory(CommandFactory factory);
    CommandFactory getCommandFactory(Class<? extends CubeCommand> type);
    void removeCommandFactory(Class clazz);
    CubeCommand getCommand(String name);
    void removeCommands(String name);
    void removeCommands(Module module);
    boolean runCommand(CommandSender sender, String commandLine);
    void removeCommands();
    ConsoleCommandSender getConsoleSender();

    void logExecution(CommandSender sender, CubeCommand cubeCommand, String[] args);
    void logTabCompletion(CommandSender sender, CubeCommand cubeCommand, String[] args);

    boolean hasPendingConfirmation(CommandSender sender);

    ConfirmResult getPendingConfirmation(CommandSender sender);

    void registerConfirmResult(ConfirmResult confirmResult, Module module, CommandSender sender);
}
