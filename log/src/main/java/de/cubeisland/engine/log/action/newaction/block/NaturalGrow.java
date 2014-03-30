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
package de.cubeisland.engine.log.action.newaction.block;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static org.bukkit.Material.AIR;

/**
 * Represents trees or mushrooms growing
 */
public class NaturalGrow extends BlockActionType<BlockListener>
{
    // return "natural-grow";
    // return this.lm.getConfig(world).block.grow.NATURAL_GROW_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof NaturalGrow && ((NaturalGrow)action).oldBlock == this.oldBlock
            && ((NaturalGrow)action).newBlock == this.newBlock;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        if (this.oldBlock.is(AIR))
        {
            return user.getTranslationN(POSITIVE, count, "{name#block} grew naturally",
                                        "{1:amount}x {name#block} grew naturally", this.newBlock.name(), count);
        }
        return user.getTranslationN(POSITIVE, count, "{name#block} grew naturally into {name#block}",
                                    "{2:amount}x {name#block} grew naturally into {name#block}", this.newBlock.name(),
                                    this.oldBlock.name(), count);
    }
}
