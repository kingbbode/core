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
package de.cubeisland.engine.core.world;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.google.common.base.Optional;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.util.Cleanable;
import org.jooq.types.UInteger;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

public interface WorldManager extends Cleanable
{
    World createWorld(WorldProperties creator);
    UInteger getWorldId(World world);
    UInteger getWorldId(String name);
    Set<UInteger> getAllWorldIds();
    World getWorld(UInteger id);
    Optional<World> getWorld(String name);
    Optional<World> getWorld(UUID uid);
    boolean unloadWorld(String worldName, boolean save);
    boolean unloadWorld(World world, boolean save);
    boolean deleteWorld(String worldName) throws IOException;
    boolean deleteWorld(World world) throws IOException;
    Set<World> getWorlds();

    WorldEntity getWorldEntity(World world);

    List<String> getWorldNames();

    Set<UUID> getAllWorldUUIDs();
}
