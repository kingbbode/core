package de.cubeisland.cubeengine.log.storage.kills;

import de.cubeisland.cubeengine.core.storage.database.AttrType;
import de.cubeisland.cubeengine.core.storage.database.Attribute;
import de.cubeisland.cubeengine.core.storage.database.DatabaseConstructor;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import de.cubeisland.cubeengine.log.storage.AbstractLog;
import java.sql.Timestamp;
import java.util.List;
import org.bukkit.World;

public class KillLog extends AbstractLog
{
    @Attribute(type = AttrType.INT)
    public int killedId; // positive values for Players / negative EntityId for mobs
    //causeID is the killer

    @DatabaseConstructor
    public KillLog(List<Object> args) throws ConversionException
    {
        this.key = Convert.fromObject(Integer.class, args.get(0));
        this.timestamp = (Timestamp)args.get(1);
        this.causeID = Convert.fromObject(Integer.class, args.get(2));
        this.world = Convert.fromObject(World.class, args.get(3));
        this.x = Convert.fromObject(Integer.class, args.get(4));
        this.y = Convert.fromObject(Integer.class, args.get(5));
        this.z = Convert.fromObject(Integer.class, args.get(6));
        this.killedId = Convert.fromObject(Integer.class, args.get(7));
    }
}
