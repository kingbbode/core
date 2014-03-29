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
package de.cubeisland.engine.log.action.newaction.block.player.interact;

import java.util.concurrent.TimeUnit;

import org.bukkit.block.BlockState;
import org.bukkit.block.NoteBlock;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;

/**
 * Represents a player changing the tune of a noteblock
 */
public class NoteBlockChange extends PlayerBlockActionType<PlayerBlockInteractListener>
{
    // return "noteblock-change";
    // return this.lm.getConfig(world).block.NOTEBLOCK_CHANGE_enable;

    public byte note;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof NoteBlockChange && this.player
            .equals(((PlayerBlockActionType)action).player) && TimeUnit.MINUTES.toMillis(2) > Math
            .abs(this.date.getTime() - action.date.getTime());
    }

    @Override
    public String translateAction(User user)
    {
        byte newNote = (byte)(this.note + 1);
        if (this.hasAttached())
        {
            newNote = (byte)(((NoteBlockChange)this.getAttached().get(this.getAttached().size() - 1)).note + 1);
        }
        newNote %= 25;
        if (this.note == newNote)
        {
            return user
                .getTranslation(MessageType.POSITIVE, "{user} fiddled around with the noteblock but did not change anything", this.player.name);
        }
        return user
            .getTranslation(MessageType.POSITIVE, "{user} set the noteblock to {amount} clicks", this.player.name, newNote);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setOldBlock(BlockState state)
    {
        super.setOldBlock(state);
        this.note = ((NoteBlock)state).getNote().getId();
    }
}
