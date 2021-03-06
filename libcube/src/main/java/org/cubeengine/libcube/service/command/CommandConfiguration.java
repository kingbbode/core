/*
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
package org.cubeengine.libcube.service.command;

import java.util.ArrayList;
import java.util.List;
import org.cubeengine.reflect.annotations.Comment;
import org.cubeengine.reflect.codec.yaml.ReflectedYaml;

public class CommandConfiguration extends ReflectedYaml
{
    @Comment("The maximum number of similar commands to offer when more than one command matches a mistyped command.")
    public int maxCorrectionOffers = 5;

    @Comment("The maximum number of offers given for a tab completion request (pressing tab).")
    public int maxTabCompleteOffers = 5;

    @Comment("A List of commands CubeEngine will not try to override")
    public List<String> noOverride = new ArrayList<>();
}
