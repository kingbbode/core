package de.cubeisland.cubeengine.basics.command.moderation;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsConfiguration;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.exception.InvalidUsageException;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.*;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
import static de.cubeisland.cubeengine.core.i18n.I18n._;
import static de.cubeisland.cubeengine.core.util.Misc.arr;

/**
 * The /spawnmob command.
 */
public class SpawnMobCommand
{
    private BasicsConfiguration config;

    public SpawnMobCommand(Basics basics)
    {
        config = basics.getConfiguration();
    }

    @Command(desc = "Spawns the specified Mob", max = 3, usage = "<mob>[:data][,<ridingmob>[:data]] [amount] [player]")
    public void spawnMob(CommandContext context)
    {
        // TODO fix skeleton not having a bow
        // TODO custom loot items
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        if (!context.hasArg(2) && sender == null)
        {
            invalidUsage(context, "basics", "&eSuccesfully spawned some &cbugs &einside your server!");
        }
        if (!context.hasArg(0))
        {
            invalidUsage(context, "basics", "&cYou need to define what mob to spawn!");
        }
        Location loc;
        if (context.hasArg(2))
        {
            User user = context.getUser(2);
            if (user == null)
            {
                illegalParameter(context, "core", "&cUser %s not found!", context.getString(2));
            }
            loc = user.getLocation();
        }
        else
        {
            loc = sender.getTargetBlock(null, 200).getLocation().add(new Vector(0, 1, 0));
        }
        Integer amount = 1;
        if (context.hasArg(1))
        {
            amount = context.getArg(1, Integer.class, null);
            if (amount == null)
            {
                illegalParameter(context, "basics", "&e%s is not a number! Really!", context.getString(1));
            }
            if (amount <= 0)
            {
                illegalParameter(context, "basics", "&eAnd how am i supposed to know which mobs to despawn?");
            }
        }
        if (amount > config.spawnmobLimit)
        {
            denyAccess(context, "basics", "&cThe serverlimit is set to &e%d&c, you cannot spawn more mobs at once!", config.spawnmobLimit);
        }
        loc.add(0.5, 0, 0.5);
        Entity entitySpawned = this.spawnMobs(context, context.getString(0), loc, amount);
        if (entitySpawned.getPassenger() == null)
        {
            context.sendMessage("basics", "&aSpawned %d &e%s&a!", amount, Match.entity().getNameFor(entitySpawned.getType()));
        }
        else
        {
            String message = Match.entity().getNameFor(entitySpawned.getType());
            while (entitySpawned.getPassenger() != null)
            {
                entitySpawned = entitySpawned.getPassenger();
                message = _(context.getSender(), "basics", "%s &ariding &e%s", arr(Match.entity().getNameFor(entitySpawned.getType()), message));
            }
            message = _(context.getSender(), "basics", "&aSpawned %d &e%s!", arr(amount, message));
            context.sendMessage(message);
        }
    }

    private Entity spawnMobs(CommandContext context, String mobString, Location loc, int amount)
    {
        String[] mobStrings = StringUtils.explode(",", mobString);
        Entity[] mobs = this.spawnMob(context, mobStrings[0], loc, amount, null);
        Entity[] ridingMobs = mobs;
        try
        {
            for (int i = 1; i < mobStrings.length; ++i)
            {
                ridingMobs = this.spawnMob(context, mobStrings[i], loc, amount, ridingMobs);
            }
            return mobs[0];
        }
        catch (InvalidUsageException e)
        {
            context.sendMessage(e.getMessage());
            return mobs[0];
        }
    }

    private Entity[] spawnMob(CommandContext context, String mobString, Location loc, int amount, Entity[] ridingOn)
    {
        String entityName = mobString;
        EntityType entityType;
        List<String> entityData = new ArrayList<String>();
        if (entityName.contains(":"))
        {
            entityData = Arrays.asList(StringUtils.explode(":", entityName.substring(entityName.indexOf(":") + 1, entityName.length())));
            entityName = entityName.substring(0, entityName.indexOf(":"));
            entityType = Match.entity().mob(entityName);
        }
        else
        {
            entityType = Match.entity().mob(entityName);
        }
        if (entityName.isEmpty())
        {
            blockCommand();
        }
        if (entityType == null)
        {
            paramNotFound(context, "basics", "&cUnknown mob-type: &6%s &cnot found!", entityName);
        }
        Entity[] spawnedMobs = new Entity[amount];
        for (int i = 0; i < amount; ++i)
        {
            spawnedMobs[i] = loc.getWorld().spawnEntity(loc, entityType);
            this.applyDataToMob(context.getSender(), entityType, spawnedMobs[i], entityData);
            if (ridingOn != null)
            {
                ridingOn[i].setPassenger(spawnedMobs[i]);
            }
        }
        return spawnedMobs;
    }

    private void applyDataToMob(CommandSender sender, EntityType entityType, Entity entity, List<String> datas)
    {
        //TODO other additional data?
        for (String data : datas)
        {
            if (data != null)
            {
                String match = Match.string().matchString(data.toLowerCase(Locale.ENGLISH), "saddled", "baby", "angry", "tamed", "power", "charged", "sitting");
                //TODO this list configurable something like datavalues.txt
                if (match == null)
                {
                    if (data.toLowerCase(Locale.ENGLISH).endsWith("hp"))
                    {
                        try
                        {
                            int hp = Integer.parseInt(data.substring(0, data.length() - 2));
                            ((LivingEntity)entity).setMaxHealth(hp);
                            ((LivingEntity)entity).setHealth(hp);
                            continue;
                        }
                        catch (NumberFormatException e)
                        {
                            sender.sendMessage("basics", "&cInvalid HP amount!");
                            return;
                        }
                    }
                }
                if ("baby".equals(match))
                {
                    if (Match.entity().isAnimal(entityType))
                    {
                        ((Animals)entity).setBaby();
                    }
                    else
                    {
                        blockCommand(sender, "basics", "&eThis entity can not be a baby! Can you?");
                    }
                }
                else if ("saddled".equals(match))
                {
                    if (entity instanceof Pig)
                    {
                        ((Pig)entity).setSaddle(true);
                    }
                    else
                    {
                        blockCommand(sender, "basics", "&eOnly Pigs can be saddled!");
                    }
                }
                else if ("angry".equals(match))
                {
                    if (entityType.equals(EntityType.WOLF))
                    {
                        ((Wolf)entity).setAngry(true);
                    }
                    else if (entityType.equals(EntityType.PIG_ZOMBIE))
                    {
                        ((PigZombie)entity).setAngry(true);
                    }
                }
                else if ("tamed".equals(match))
                {
                    if (entity instanceof Tameable) // Wolf or Ocelot
                    {

                        ((Tameable)entity).setTamed(true);
                        if (sender instanceof AnimalTamer)
                        {
                            ((Tameable)entity).setOwner((AnimalTamer)sender);
                        }
                        else
                        {
                            blockCommand(sender, "basics", "&eYou can not own any Animals!");
                        }
                    }
                }
                else if ("sitting".equals(match))
                {
                    if (entity instanceof Wolf)
                    {
                        ((Wolf)entity).setSitting(true);
                    }
                    else if (entity instanceof Ocelot)
                    {
                        ((Ocelot)entity).setSitting(true);
                    }
                    else
                    {
                        blockCommand(sender, "basics", "&eOnly a wolfs and ocelots can sit!");
                    }
                }
                else if ("charged".equals(match) || data.equalsIgnoreCase("power"))
                {
                    if (entityType.equals(EntityType.CREEPER))
                    {
                        ((Creeper)entity).setPowered(true);
                    }
                    else
                    {
                        blockCommand(sender, "basics", "&eYou can only charge creepers!");
                    }
                }
                else if (entityType.equals(EntityType.SHEEP))
                {
                    DyeColor color = Match.materialData().colorData(data);
                    if (color == null)
                    {
                        blockCommand(sender, "basics", "&cInvalid SheepColor: " + data);
                    }
                    ((Sheep)entity).setColor(color);
                }
                else if (entityType.equals(EntityType.SLIME) || entityType.equals(EntityType.MAGMA_CUBE))
                {
                    int size = 4;
                    match = Match.string().matchString(data, "tiny", "small", "big");
                    if ("tiny".equals(match))
                    {
                        size = 0;
                    }
                    else if ("small".equals(match))
                    {
                        size = 2;
                    }
                    else if ("big".equals(match))
                    {
                        size = 4;
                    }
                    else
                    {
                        try
                        {
                            size = Integer.parseInt(data);
                        }
                        catch (NumberFormatException e)
                        {
                            illegalParameter(sender, "basics", "&eThe slime-size has to be a number or tiny, small or big!");
                        }
                    }
                    if (size >= 0 && size <= 250)
                    {
                        ((Slime)entity).setSize(size);
                    }
                    else
                    {
                        illegalParameter(sender, "basics", "&eThe slime-size can not be smaller than 0 or bigger than 250!");
                    }
                }
                else if (entityType.equals(EntityType.VILLAGER))
                {
                    Villager.Profession profession = Match.profession().profession(data);
                    if (profession == null)
                    {
                        illegalParameter(sender, "basics", "Unknown villager-profession!");
                    }
                    ((Villager)entity).setProfession(profession);
                }
                else if (entityType.equals(EntityType.ENDERMAN))
                {
                    ItemStack item = Match.material().itemStack(data);
                    if (item == null)
                    {
                        illegalParameter(sender, "basics", "Material not found!");
                    }
                    ((Enderman)entity).setCarriedMaterial(item.getData());
                }
            }
        }
    }
}