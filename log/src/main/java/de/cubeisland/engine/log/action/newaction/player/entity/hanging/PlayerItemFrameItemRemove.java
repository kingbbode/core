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
package de.cubeisland.engine.log.action.newaction.player.entity.hanging;

import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.newaction.BaseAction;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.ITEM;

/**
 * Represents a player removing an item from an item-frame
 */
public class PlayerItemFrameItemRemove extends PlayerHangingAction
{
    // return this.lm.getConfig(world).ITEM_REMOVE_FROM_FRAME;

    public ItemStack item;

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof PlayerItemFrameItemRemove && this.player.equals(
            ((PlayerItemFrameItemRemove)action).player);
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count, "{user} removed {name#item} from an itemframe",
                                    "{user} removed {2:amount} items from itemframes", this.player.name,
                                    this.item.getType().name(), count);
    }

    // TODO redo/rollback


    @Override
    public ActionCategory getCategory()
    {
        return ITEM;
    }

    @Override
    public String getName()
    {
        return "remove";
    }
}
