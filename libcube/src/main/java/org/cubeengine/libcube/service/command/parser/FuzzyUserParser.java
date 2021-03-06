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
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.parameter.argument.ArgumentParser;
import org.cubeengine.butler.parameter.argument.ParserException;
import org.cubeengine.libcube.service.command.TranslatedParserException;
import org.cubeengine.libcube.service.i18n.I18n;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;

import static java.util.stream.Collectors.toList;
import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NEGATIVE;

/**
 * Matches exact offline players and online players using * for wildcard
 */
public class FuzzyUserParser implements ArgumentParser<List<Player>>
{

    private final Game game;
    private final I18n i18n;

    public FuzzyUserParser(Game game, I18n i18n)
    {
        this.game = game;
        this.i18n = i18n;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Player> parse(Class type, CommandInvocation invocation) throws ParserException
    {
        ArrayList<Player> users = new ArrayList<>();
        if ("*".equals(invocation.currentToken()))
        {
            invocation.consume(1);
            users.addAll(game.getServer().getOnlinePlayers());
            return users;
        }
        if (invocation.currentToken().contains(","))
        {
            ((List<List<Player>>)invocation.providers().parsers().get(List.class).parse(FuzzyUserParser.class, invocation))
                .forEach(users::addAll);
            return users;
        }
        String token = invocation.currentToken();
        if (token.contains("*"))
        {
            Pattern pattern = Pattern.compile(token.replace("*", ".*"));
            users.addAll(game.getServer().getOnlinePlayers().stream()
                           .filter(user -> pattern.matcher(user.getName()).matches())
                           .collect(toList()));
            if (users.isEmpty())
            {
                throw new TranslatedParserException(i18n.translate(invocation.getContext(Locale.class), NEGATIVE, "Player {user} not found!", token));
            }
            invocation.consume(1);
        }
        else
        {
            users.add((Player)invocation.providers().read(Player.class, Player.class, invocation));
        }
        return users;
    }
}
