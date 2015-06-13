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
package de.cubeisland.engine.module.service.ban;

import java.net.InetAddress;
import java.util.Set;
import java.util.UUID;
import de.cubeisland.engine.modularity.asm.marker.Service;
import de.cubeisland.engine.modularity.asm.marker.Version;

@Service
@Version(1)
public interface BanManager
{
    void addBan(Ban ban);

    UserBan getUserBan(UUID uuid);

    IpBan getIpBan(InetAddress address);

    boolean removeUserBan(UUID uuid);

    boolean removeIpBan(InetAddress address);

    boolean isUserBanned(UUID uuid);

    boolean isIpBanned(InetAddress address);

    Set<UserBan> getUserBans();

    Set<IpBan> getIpBans();

    Set<Ban<?>> getBans();

    void reloadBans();
}