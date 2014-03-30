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
package de.cubeisland.engine.log.action.newaction.player.item;

import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.player.PlayerActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player crafting an item
 */
public class CraftItem extends PlayerActionType<PlayerItemListener>
{
    // return "craft-item";
    // return this.lm.getConfig(world).CRAFT_ITEM_enable;

    public ItemStack craftItem; // TODO item format

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof CraftItem && this.player.equals(((CraftItem)action).player)
            && ((CraftItem)action).craftItem.isSimilar(this.craftItem);
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            return user.getTranslation(POSITIVE, "{user} crafted {name#item} x{amount}", this.player.name,
                                       this.craftItem.getType().name(), this.getAttached().size() + 1);
        }
        return user.getTranslation(POSITIVE, "{user} crafted {name#item}", this.player.name,
                                   this.craftItem.getType().name());
    }

    public void setItem(ItemStack result)
    {
        this.craftItem = result;
    }
}
