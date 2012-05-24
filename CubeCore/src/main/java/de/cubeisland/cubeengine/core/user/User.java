package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.persistence.Model;
import de.cubeisland.cubeengine.core.util.bitmask.LongBitMask;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Phillip Schichtel
 */
public class User extends UserBase implements Model
{
    private final OfflinePlayer player;
    private final LongBitMask flags;
    private int id;
    private String language;
    
    
    public static final int BLOCK_FLY = 1;
    
    
    public User(int id, OfflinePlayer player, LongBitMask flags, String language)
    {
        super(player);
        this.id = id;
        this.player = player;
        this.flags = flags;
        this.language = language;
    }
    
    public User(OfflinePlayer player)
    {
        this(-1, player, new LongBitMask(), "en");//TODO assign correct Language ?
    }

    /**
     * @return the offlineplayer
     */
    public OfflinePlayer getOfflinePlayer()
    {
        return this.player;
    }
    
    /**
     * @return the flags
     */
    public LongBitMask getFlags()
    {
        return this.flags;
    }

    /**
     * @return the CubeUsers ID
     */
    public int getId()
    {
        return this.id;
    }
    
    /**
     * @param id the id to set
     */
    public void setId(int id)
    {
        this.id = id;
    }
    
    public boolean hasFlag(int flag)
    {
        return flags.isset(flag);
    }
    
    public boolean toggleFlag (int flag)
    {
        flags.toggle(flag);
        return this.hasFlag(flag);
    }
    
    public void setFlag(int flag)
    {
        flags.set(flag);
    }
    
    public void unsetFlag(int flag)
    {
        flags.unset(flag);
    }
    
    public void setLanguage(String lang)
    {
        this.language = lang;
    }
    
    public String getLanguage()
    {
        return this.language;
    }
}
