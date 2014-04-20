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
package de.cubeisland.engine.core.recipe.result.item;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.recipe.NoMoreSpaceException;
import de.cubeisland.engine.core.recipe.result.logic.Result;
import de.cubeisland.engine.core.util.InventoryUtil;

public class AdditionalItemResult extends Result
{
    private ItemStack item;

    public AdditionalItemResult(ItemStack item)
    {
        this.item = item;
    }

    @Override
    public ItemStack getResult(Player player, BlockState block, ItemStack itemStack)
    {
        if (InventoryUtil.addItemsToInventory(player.getInventory(), this.item))
        {
            return itemStack;
        }
        throw new NoMoreSpaceException();
    }
}