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
package de.cubeisland.engine.log.action.newaction.block.player.bucket;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockPlace;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player emptying a lavabucket
 * <p>Listener:
 * {@link PlayerBucketListener}
 */
public class PlayerLavaBucketPlace extends PlayerBlockPlace
{
    // return this.lm.getConfig(world).block.bucket.LAVA_BUCKET_enable;

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            return user.getTranslation(POSITIVE, "{user} emptied {amount} lava-buckets", this.player.name,
                                       this.countAttached());
        }
        return user.getTranslation(POSITIVE, "{user} emptied a lava-bucket", this.player.name);
    }

    @Override
    public ActionCategory getCategory()
    {
        return ActionCategory.BUCKET;
    }

    @Override
    public String getName()
    {
        return "lava";
    }
}
