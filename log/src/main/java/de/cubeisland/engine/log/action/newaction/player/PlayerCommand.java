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
package de.cubeisland.engine.log.action.newaction.player;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.newaction.BaseAction;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.PLAYER;

/**
 * Represents a player executing a command
 */
public class PlayerCommand extends PlayerAction<PlayerActionListener>
{
    // return this.lm.getConfig(world).PLAYER_COMMAND_enable;

    private String command;

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof PlayerCommand && this.player.equals(((PlayerCommand)action).player)
            && ((PlayerCommand)action).command.equalsIgnoreCase(this.command);
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            return user.getTranslation(POSITIVE, "{user} used the command \"{input#command}\" x{amount}",
                                       this.player.name, this.command, this.getAttached().size() + 1);
        }
        return user.getTranslation(POSITIVE, "{user} used the command \"{input#command}\"", this.player.name,
                                   this.command);
    }

    public String getCommand()
    {
        return command;
    }

    public void setCommand(String command)
    {
        this.command = command;
    }

    @Override
    public ActionCategory getCategory()
    {
        return PLAYER;
    }

    @Override
    public String getName()
    {
        return "command";
    }
}
