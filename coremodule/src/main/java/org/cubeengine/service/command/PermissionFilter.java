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

import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.filter.Filter;
import de.cubeisland.engine.butler.filter.FilterException;
import org.cubeengine.service.command.exception.PermissionDeniedException;
import org.cubeengine.service.command.property.RawPermission;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.command.CommandSource;

/**
 * A Filter checking a CommandSource Permission.
 * If the CommandSource is not a Subject TODO ... what to do what to do
 */
public class PermissionFilter implements Filter
{
    private RawPermission permission;

    public PermissionFilter(RawPermission permission)
    {
        this.permission = permission;
    }

    @Override
    public void run(CommandInvocation invocation) throws FilterException
    {
        Object source = invocation.getCommandSource();
        if (source instanceof Subject)
        {
            if (!((Subject)source).hasPermission(permission.getName()))
            {
                throw new PermissionDeniedException(permission);
            }
            return;
        }
        throw new PermissionDeniedException(permission);
    }
}
