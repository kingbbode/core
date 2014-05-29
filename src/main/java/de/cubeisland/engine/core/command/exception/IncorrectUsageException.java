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
package de.cubeisland.engine.core.command.exception;

/**
 * This exception is thrown when a user performed an invalid command.
 * Use invalidUsage to throw an exception inside a command. The exception will be caught.
 */
public class IncorrectUsageException extends CommandException
{
    private final boolean displayUsage;
    
    public IncorrectUsageException()
    {
        this(null, true);
    }

    public IncorrectUsageException(String message)
    {
        this(message, true);
    }

    public IncorrectUsageException(String message, boolean displayUsage)
    {
        super(message);
        this.displayUsage = displayUsage;
    }

    public boolean getDisplayUsage()
    {
        return displayUsage;
    }
}