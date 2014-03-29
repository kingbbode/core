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
package de.cubeisland.engine.bigdata;

import de.cubeisland.engine.reflect.Reflected;
import org.bson.types.ObjectId;

public class ReflectedMongoDB extends Reflected<MongoDBCodec, RDBObject>
{
    transient ObjectId _id;

    public ObjectId getId()
    {
        return _id;
    }

    @Override
    public void save(RDBObject rdbo)
    {
        this.getCodec().saveReflected(this, rdbo);
    }

    @Override
    public boolean loadFrom(RDBObject rdbo)
    {
        this.getCodec().loadReflected(this, rdbo);
        return true;
    }
}
