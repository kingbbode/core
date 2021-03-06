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
package org.cubeengine.libcube.service.command.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.parameter.argument.Completer;
import org.cubeengine.butler.parameter.argument.ArgumentParser;
import org.cubeengine.butler.parameter.argument.ParserException;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.translation.Translatable;

public abstract class CatalogTypeParser<T extends CatalogType> implements ArgumentParser<T>, Completer
{
    private Class<T> type;

    public CatalogTypeParser(Class<T> type)
    {
        this.type = type;
    }

    @Override
    public T parse(Class type, CommandInvocation invocation) throws ParserException
    {
        String token = invocation.currentToken();
        for (T cType : getAllOf())
        {
            String name = cType.getName().replace(" ", "_");
            if (name.equalsIgnoreCase(token))
            {
                invocation.consume(1);
                return cType;
            }
            if (cType instanceof Translatable)
            {
                name = ((Translatable) cType).getTranslation().get(invocation.getContext(Locale.class)).replace(" ", "_");
                if (name.equalsIgnoreCase(token))
                {
                    invocation.consume(1);
                    return cType;
                }
            }
        }
        throw new ParserException("Could not find CatalogType: '" + token + "' in " + this.type.getName());
    }

    protected Collection<T> getAllOf()
    {
        return new ArrayList<>(Sponge.getRegistry().getAllOf(this.type));
    }

    @Override
    public List<String> suggest(Class type, CommandInvocation invocation)
    {
        List<String> list = new ArrayList<>();
        String token = invocation.currentToken().toLowerCase();
        for (T cType : getAllOf())
        {
            String name;
            if (cType instanceof Translatable)
            {
                name = ((Translatable) cType).getTranslation().get(invocation.getContext(Locale.class)).replace(" ", "_");
            }
            else
            {
                name = cType.getName().replace(" ", "_");
            }


            if (name.toLowerCase().startsWith(token))
            {
                list.add(name);
            }
        }
        return list;
    }
}
