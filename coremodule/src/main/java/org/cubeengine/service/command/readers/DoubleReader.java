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
package org.cubeengine.service.command.readers;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.parameter.reader.ArgumentReader;
import de.cubeisland.engine.butler.parameter.reader.ReaderException;
import org.cubeengine.service.command.TranslatedReaderException;
import org.cubeengine.service.i18n.I18n;
import org.cubeengine.service.i18n.formatter.MessageType;

public class DoubleReader implements ArgumentReader<Double>
{
    private I18n i18n;

    public DoubleReader(I18n i18n)
    {

        this.i18n = i18n;
    }

    @Override
    public Double read(Class type, CommandInvocation invocation) throws ReaderException
    {
        String arg = invocation.consume(1);
        Locale locale = invocation.getContext(Locale.class);
        try
        {
            return NumberFormat.getInstance(locale).parse(arg).doubleValue();
        }
        catch (ParseException e)
        {
            try
            {
                return NumberFormat.getInstance().parse(arg).doubleValue(); // Try parsing with default locale
            }
            catch (ParseException e1)
            {
                throw new TranslatedReaderException(i18n.translate(locale, MessageType.NEGATIVE,
                                                         "Could not parse {input} to double!", arg));
            }
        }
    }
}
