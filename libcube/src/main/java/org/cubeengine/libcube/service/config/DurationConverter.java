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
package org.cubeengine.libcube.service.config;

import org.cubeengine.converter.ConversionException;
import org.cubeengine.converter.converter.SimpleConverter;
import org.cubeengine.converter.node.IntNode;
import org.cubeengine.converter.node.Node;
import org.cubeengine.converter.node.StringNode;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class DurationConverter extends SimpleConverter<Duration>
{
    private final PeriodFormatter formatter;

    public DurationConverter()
    {
        this.formatter = new PeriodFormatterBuilder()
        .appendWeeks().appendSuffix("w").appendSeparator(" ")
        .appendDays().appendSuffix("d").appendSeparator(" ")
        .appendHours().appendSuffix("h").appendSeparator(" ")
        .appendMinutes().appendSuffix("m").appendSeparator(" ")
        .appendSeconds().appendSuffix("s").appendSeparator(".")
        .appendMillis().toFormatter();
    }

    @Override
    public Node toNode(Duration object) throws ConversionException
    {
        return StringNode.of(this.formatter.print(object.toPeriod()));
    }

    @Override
    public Duration fromNode(Node node) throws ConversionException
    {
        try
        {
            if (node instanceof IntNode)
            {
                return new Duration(((IntNode)node).getValue().longValue());
            }
            return this.formatter.parsePeriod(node.asText()).toStandardDuration();
        }
        catch (Exception e)
        {
            throw ConversionException.of(this, node, "Unknown error while parsing Duration!", e);
        }
    }
}
