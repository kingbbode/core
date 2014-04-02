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
package de.cubeisland.engine.log.action.newaction.player.entity.hanging;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.newaction.BaseAction;
import de.cubeisland.engine.log.action.newaction.LogListener;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockAction;
import de.cubeisland.engine.log.action.newaction.player.entity.hanging.destroy.PlayerHangingBreak;
import de.cubeisland.engine.log.action.newaction.player.entity.hanging.destroy.PlayerItemFrameBreak;
import de.cubeisland.engine.log.action.newaction.player.entity.hanging.destroy.PlayerPaintingBreak;

/**
 * A Listener for PlayerHanging Actions
 * <p>Events:
 * {@link HangingBreakEvent}
 * {@link HangingBreakByEntityEvent}
 * {@link HangingPlaceEvent}
 * {@link EntityDamageByEntityEvent}
 * <p>PrePlan Events:
 * {@link HangingPreBreakEvent}
 * <p>Actions:
 * {@link PlayerHangingBreak}
 * {@link PlayerItemFrameBreak}
 * {@link PlayerPaintingBreak}
 * {@link PlayerHangingPlace}
 * {@link PlayerPaintingPlace}
 * {@link PlayerItemFrameItemRemove}
 * <p>External Actions:
 * TODO {@link OtherHangingBreak}
 * TODO {@link OtherPaintingBreak}
 * TODO {@link OtherItemframeBreak}
 * TODO {@link OtherItemFrameItemRemove}
 */
public class PlayerHangingListener extends LogListener
{
    // TODO place item in frame

    private final Map<Location, BaseAction> plannedHangingBreak = new ConcurrentHashMap<>();
    private volatile boolean clearPlanned = false;

    public PlayerHangingListener(Log module)
    {
        super(module, PlayerHangingBreak.class, PlayerItemFrameBreak.class, PlayerPaintingBreak.class,
              PlayerHangingPlace.class, PlayerPaintingPlace.class, PlayerItemFrameItemRemove.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event)
    {
        if (event.getCause() == HangingBreakEvent.RemoveCause.PHYSICS)
        {
            Hanging hanging = event.getEntity();
            Location location = hanging.getLocation();
            BaseAction cause = this.plannedHangingBreak.get(location);
            if (cause != null)
            {
                if (cause instanceof PlayerBlockAction)
                {
                    PlayerHangingBreak action;
                    if (hanging instanceof ItemFrame)
                    {
                        action = this.newAction(PlayerItemFrameBreak.class, location.getWorld());
                        ItemStack item = ((ItemFrame)hanging).getItem();
                        if (action != null && item != null)
                        {
                            ((PlayerItemFrameBreak)action).item = item;
                        }
                    }
                    else if (hanging instanceof Painting)
                    {
                        action = this.newAction(PlayerPaintingBreak.class, location.getWorld());
                        ((PlayerPaintingBreak)action).art = ((Painting)hanging).getArt();
                    }
                    else
                    {
                        action = this.newAction(PlayerHangingBreak.class, location.getWorld());
                    }
                    if (action != null)
                    {
                        action.setLocation(location);
                        action.setHanging(hanging);
                        action.player = ((PlayerBlockAction)cause).player;
                        action.setCause(cause);
                        this.logAction(action);
                    }
                }
                // else // TODO
            }
            // else TODO this.module.getLog().info("Unexpected HangingBreakEvent");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event)
    {
        Entity causer;
        if (event.getRemover() instanceof Projectile)
        {
            ProjectileSource shooter = ((Projectile)event.getRemover()).getShooter();
            if (shooter instanceof Entity)
            {
                causer = (Entity)shooter;
            }
            else
            {
                return; // TODO other ProjectileSources
            }
        }
        else
        {
            causer = event.getRemover();
        }

        Hanging hanging = event.getEntity();
        Location location = hanging.getLocation();
        if (causer instanceof Player)
        {
            PlayerHangingBreak action;
            if (hanging instanceof ItemFrame)
            {
                action = this.newAction(PlayerItemFrameBreak.class, location.getWorld());
                ItemStack item = ((ItemFrame)hanging).getItem();
                if (action != null && item != null)
                {
                    ((PlayerItemFrameBreak)action).item = item;
                }
            }
            else if (hanging instanceof Painting)
            {
                action = this.newAction(PlayerPaintingBreak.class, location.getWorld());
                ((PlayerPaintingBreak)action).art = ((Painting)hanging).getArt();
            }
            else
            {
                action = this.newAction(PlayerHangingBreak.class, location.getWorld());
            }
            if (action != null)
            {
                action.setLocation(location);
                action.setHanging(hanging);
                action.setPlayer((Player)causer);
                this.logAction(action);
            }
        }
        else
        {
            // TODO
        }
    }

    @EventHandler
    public void onPreHangingBreak(HangingPreBreakEvent event) // TODO
    {
        plannedHangingBreak.put(event.getLocation(), event.getCause());
        if (!clearPlanned)
        {
            clearPlanned = true;
            module.getCore().getTaskManager().runTask(module, new Runnable()
            {
                @Override
                public void run()
                {
                    clearPlanned = false;
                    plannedHangingBreak.clear();
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event)
    {
        Hanging hanging = event.getEntity();
        PlayerHangingPlace action;
        if (hanging instanceof Painting)
        {
            action = this.newAction(PlayerPaintingPlace.class, hanging.getWorld());
            ((PlayerPaintingPlace)action).art = ((Painting)hanging).getArt();
        }
        else
        {
            action = this.newAction(PlayerHangingPlace.class, hanging.getWorld());
        }
        if (action != null)
        {
            action.setLocation(hanging.getLocation());
            action.setHanging(hanging);
            action.setPlayer(event.getPlayer());
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemRemove(EntityDamageByEntityEvent event)
    {
        Entity frame = event.getEntity();
        if (frame instanceof ItemFrame)
        {
            Entity causer;
            if (event.getDamager() instanceof Projectile)
            {
                ProjectileSource shooter = ((Projectile)event.getDamager()).getShooter();
                if (shooter instanceof Entity)
                {
                    causer = (Entity)shooter;
                }
                else
                {
                    return; // TODO other ProjectileSources
                }
            }
            else
            {
                causer = event.getDamager();
            }

            if (causer instanceof Player)
            {
                PlayerItemFrameItemRemove action = this.newAction(PlayerItemFrameItemRemove.class, frame.getWorld());
                if (action != null)
                {
                    action.setLocation(frame.getLocation());
                    action.setHanging(frame);
                    action.setPlayer((Player)causer);
                    action.item = ((ItemFrame)frame).getItem();
                    this.logAction(action);
                }
            }
            else
            {
                // TODO
            }
        }
    }
}
