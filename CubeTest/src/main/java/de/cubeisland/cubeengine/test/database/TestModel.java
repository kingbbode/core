package de.cubeisland.cubeengine.test.database;

import de.cubeisland.cubeengine.core.persistence.AttrType;
import de.cubeisland.cubeengine.core.persistence.Attribute;
import de.cubeisland.cubeengine.core.persistence.Entity;
import de.cubeisland.cubeengine.core.persistence.Key;

/**
 *
 * @author Anselm Brehme
 */
@Entity(name = "test")
public class TestModel
{
    @Key
    @Attribute(type = AttrType.INT, unsigned = true)
    public int id = -1;
    
    @Attribute(name = "teststring", type = AttrType.VARCHAR)
    public String stringvalue = "Cookies";
    
    @Attribute(name= "testbool", type = AttrType.BOOLEAN)
    public boolean boolvalue = true;
    
    public Integer getKey()
    {
        return this.id;
    }

    public void setKey(Integer key)
    {
        this.id = key;
    }
}