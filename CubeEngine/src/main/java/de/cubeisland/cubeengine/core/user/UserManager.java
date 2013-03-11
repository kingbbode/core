package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.Cleanable;
import de.cubeisland.cubeengine.core.util.Triplet;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import gnu.trove.set.hash.THashSet;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static de.cubeisland.cubeengine.core.i18n.I18n._;
import static de.cubeisland.cubeengine.core.logger.LogLevel.WARNING;

/**
 * This Manager provides methods to access the Users and saving/loading from
 * database.
 */
public class UserManager implements Cleanable
{
    private final BukkitCore core;
    private final UserStorage storage;
    private final List<User> onlineUsers;
    private final ConcurrentHashMap<String, User> cachedUsers;
    private final Set<Class<? extends UserAttachment>> defaultAttachments;
    private final TObjectIntMap<String> scheduledForRemoval;
    public String salt;
    private final MessageDigest messageDigest;
    private final ScheduledExecutorService nativeScheduler;

    public UserManager(final BukkitCore core)
    {
        this.storage = new UserStorage(core);
        this.core = core;

        this.cachedUsers = new ConcurrentHashMap<String, User>();
        this.onlineUsers = new CopyOnWriteArrayList<User>();

        final long delay = (long)core.getConfiguration().userManagerCleanup;
        this.nativeScheduler = Executors.newSingleThreadScheduledExecutor(core.getTaskManager().getThreadFactory());
        this.nativeScheduler.scheduleAtFixedRate(new UserCleanupTask(), delay, delay, TimeUnit.MINUTES);
        core.getServer().getPluginManager().registerEvents(new UserListener(), core);

        this.defaultAttachments = new THashSet<Class<? extends UserAttachment>>();
        this.scheduledForRemoval = new TObjectIntHashMap<String>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
        this.loadSalt();

        try
        {
            messageDigest = MessageDigest.getInstance("SHA-512");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("SHA-512 hash algorithm not available!");
        }
    }

    public boolean login(User user, String password)
    {
        if (!user.isLoggedIn())
        {
            user.loggedInState = this.checkPassword(user, password);
        }
        core.getEventManager().fireEvent(new UserAuthorizedEvent(this.core, user));
        return user.isLoggedIn();
    }

    public boolean checkPassword(User user, String password)
    {
        synchronized (this.messageDigest)
        {
            messageDigest.reset();
            password += this.salt;
            password += user.firstseen.toString();
            return Arrays.equals(user.passwd, messageDigest.digest(password.getBytes()));
        }
    }

    public void setPassword(User user, String password)
    {
        synchronized (this.messageDigest)
        {
            this.messageDigest.reset();
            password += this.salt;
            password += user.firstseen.toString();
            user.passwd = this.messageDigest.digest(password.getBytes());
            this.storage.update(user);
        }
    }

    public void resetPassword(User user)
    {
        user.passwd = null;
        this.storage.update(user);
    }

    public void resetAllPasswords()
    {
        this.storage.resetAllPasswords();
    }

    /**
     * Adds a new User
     *
     * @return the created User
     */
    private User createUser(String name)
    {
        User inUse = this.cachedUsers.get(name);
        if (inUse != null)
        {
            //User was already added
            return inUse;
        }
        User user = new User(this.core, name);
        this.cachedUsers.put(user.getName(), user);
        this.storage.store(user, false);
        this.attachDefaults(user);
        return user;
    }

    private synchronized void attachDefaults(User user)
    {
        for (Class<? extends UserAttachment> attachmentClass : this.defaultAttachments)
        {
            user.attach(attachmentClass);
        }
    }

    /**
     * Removes the user permanently. Data cannot be restored later on
     *
     * @param user the User
     * @return fluent interface
     */
    public UserManager removeUser(final User user)
    {
        this.storage.delete(user); //this is async
        this.cachedUsers.remove(user.getName());
        return this;
    }

    /**
     * Gets a User by name
     *
     * @param name the name
     * @return the User
     */
    public User getUser(String name, boolean createIfMissing)
    {
        if (name == null)
        {
            return null;
        }
        User user = this.cachedUsers.get(name);
        if (user == null)
        {
            user = this.storage.loadUser(name);
            if (user != null)
            {
                this.cachedUsers.put(name, user);
            }
        }
        if (user == null && createIfMissing)
        {
            user = this.createUser(name);
        }
        return user;
    }

    /**
     * Gets a User by Offlineplayer (creates new User if not found)
     *
     * @param player the player
     * @return the User
     */
    public User getExactUser(OfflinePlayer player)
    {
        if (player == null)
        {
            return null;
        }
        if (player instanceof User)
        {
            return (User)player;
        }
        if (!player.hasPlayedBefore())
        {
            return null;
        }
        return this.getUser(player.getName(), true);
    }

    /**
     * Gets a User by Player (creates new User if not found)
     *
     * @param player the player
     * @return the User
     */
    public User getExactUser(Player player)
    {
        if (player == null)
        {
            return null;
        }
        if (player instanceof User)
        {
            return (User)player;
        }
        return this.getUser(player.getName(), true);
    }

    /**
     * Gets a User by CommandSender (creates new User if not found)
     *
     * @param sender the sender
     * @return the User OR null if sender is not a Player
     */
    public User getExactUser(CommandSender sender)
    {
        if (sender == null)
        {
            return null;
        }
        if (sender instanceof User)
        {
            return (User)sender;
        }
        if (sender instanceof Player)
        {
            return this.getUser(sender.getName(), true);
        }
        return null;
    }

    /**
     * Gets a User by Key in DB
     *
     * @param key the key to get the user by
     * @return the user or null if not found
     */
    public User getUser(Long key)
    {
        if (key == null)
        {
            return null;
        }
        User user = this.storage.get(key);
        if (user == null)
        {
            return null;
        }
        User savedUser = this.cachedUsers.get(user.getName());
        if (savedUser == null)
        {
            this.cachedUsers.put(user.getName(), user);
            return user;
        }
        return savedUser;
    }

    /**
     * Returns all the users that are currently online
     *
     * @return a unmodifiable List of players
     */
    public List<User> getOnlineUsers()
    {
        return Collections.unmodifiableList(this.onlineUsers);
    }

    public Collection<User> getLoadedUsers()
    {
        return this.cachedUsers.values();
    }

    @Override
    public void clean()
    {
        this.storage.cleanup();
    }

    public void shutdown()
    {
        this.clean();

        this.scheduledForRemoval.forEachEntry(new TObjectIntProcedure<String>() {
            @Override
            public boolean execute(String a, int b)
            {
                core.getServer().getScheduler().cancelTask(b);
                return true;
            }
        });

        this.cachedUsers.clear();
        this.removeDefaultAttachments();
        this.nativeScheduler.shutdown();
        try
        {
            this.nativeScheduler.awaitTermination(2, TimeUnit.SECONDS);
            this.nativeScheduler.shutdownNow();
        }
        catch (InterruptedException ignore)
        {}
    }

    /**
     * Finds an online User
     *
     * @param name the name
     * @return a online User
     */
    public User findOnlineUser(String name)
    {
        User user = this.findUser(name);
        if (user != null && user.isOnline())
        {
            return user;
        }
        return null;
    }

    /**
     * Finds an User (can create a new User if a found player is online but not
     * yet added)
     *
     * @param name the name
     * @return a User
     */
    public User findUser(String name)
    {
        //Looking up loaded users
        if (name == null)
        {
            return null;
        }
        User user = this.cachedUsers.get(name);
        if (user == null)
        {
            //Looking up saved users
            user = this.storage.loadUser(name);
            if (user != null)
            {
                this.cachedUsers.put(name, user);
            }
            if (user == null) //then NO user with exact name
            {
                //Get all online Player and searching for similar names
                ArrayList<String> onlinePlayerList = new ArrayList<String>();
                for (Player player : this.core.getServer().getOnlinePlayers())
                {
                    onlinePlayerList.add(player.getName());
                }
                user = this.getUser(Match.string().matchString(name, onlinePlayerList), true);
            }
            if (user != null)
            {
                this.cachedUsers.put(user.getName(), user);//Adds User to loaded users
            }
        }
        return user;
    }

    public void broadcastMessage(String category, String message, Permission perm, Object... args)
    {
        for (Player player : this.core.getServer().getOnlinePlayers())
        {
            if (perm == null || perm.isAuthorized(player))
            {
                this.getExactUser(player).sendMessage(category, message, args);
            }
        }
        Bukkit.getServer().getConsoleSender().sendMessage(_(category, message, args));
    }

    public void broadcastMessage(String category, String message, Object... args)
    {
        this.broadcastMessage(category, message, null, args);
    }

    public void broadcastStatus(String message, String username)
    {
        message = ChatFormat.parseFormats(message);
        for (Player player : this.core.getServer().getOnlinePlayers())
        {
            this.getExactUser(player).sendMessage("* &2" + username + " &f" + message); // not yet configurable
        }
    }

    public void broadcastStatus(String category, String message, String username, Object... args)
    {
        message = "* &2" + username + " &f" + message;
        this.broadcastMessage(category, message, args);
    }

    private void loadSalt()
    {
        File file = new File(this.core.getFileManager().getDataFolder(), ".salt");
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            this.salt = reader.readLine();
            reader.close();
        }
        catch (FileNotFoundException e)
        {
            if (this.salt == null)
            {
                try
                {
                    this.salt = RandomStringUtils.randomAscii(32);
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(this.salt);
                    fileWriter.close();
                }
                catch (Exception inner)
                {
                    throw new IllegalStateException("Could not store the static salt in '" + file.getAbsolutePath() + "'!", inner);
                }
            }
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Could not store the static salt in '" + file.getAbsolutePath() + "'!", e);
        }
        FileManager.hideFile(file);
        file.setReadOnly();
    }

    private TLongObjectHashMap<Triplet<Long, String, Integer>> failedLogins = new TLongObjectHashMap<Triplet<Long, String, Integer>>();

    public Triplet<Long, String, Integer> getFailedLogin(User user)
    {
        return this.failedLogins.get(user.key);
    }

    public void addFailedLogin(User user)
    {
        Triplet<Long, String, Integer> loginFail = this.getFailedLogin(user);
        if (loginFail == null)
        {
            loginFail = new Triplet<Long, String, Integer>(System.currentTimeMillis(), user.getAddress().getAddress().getHostAddress(), 1);
            this.failedLogins.put(user.key, loginFail);
        }
        else
        {
            loginFail.setFirst(System.currentTimeMillis());
            loginFail.setSecond(user.getAddress().getAddress().getHostAddress());
            loginFail.setThird(loginFail.getThird() + 1);
        }
    }

    public void removeFailedLogins(User user)
    {
        this.failedLogins.remove(user.key);
    }

    public void kickAll(String message)
    {
        for (User user : this.cachedUsers.values())
        {
            user.kickPlayer(message);
        }
    }

    public void kickAll(String category, String message, Object... args)
    {
        for (User user : this.cachedUsers.values())
        {
            user.kickPlayer(_(user, category, message, args));
        }
    }

    public void attachToAll(Class<? extends UserAttachment> attachmentClass)
    {
        for (Player player : this.core.getServer().getOnlinePlayers())
        {
            this.getExactUser(player).attach(attachmentClass);
        }
    }

    public void detachFromAll(Class<? extends UserAttachment> attachmentClass)
    {
        for (Player player : this.core.getServer().getOnlinePlayers())
        {
            this.getExactUser(player).detach(attachmentClass);
        }
    }

    public synchronized void addDefaultAttachment(Class<? extends UserAttachment> attachmentClass)
    {
        this.defaultAttachments.add(attachmentClass);
    }

    public synchronized void removeDefaultAttachment(Class<? extends UserAttachment> attachmentClass)
    {
        this.defaultAttachments.remove(attachmentClass);
    }

    public synchronized void removeDefaultAttachments()
    {
        this.defaultAttachments.clear();
    }

    private class UserListener implements Listener
    {
        /**
         * Removes the user from loaded UserList when quitting the server and
         * updates lastseen in database
         *
         * @param event the PlayerQuitEvent
         */
        @EventHandler(priority = EventPriority.MONITOR)
        public void onQuit(final PlayerQuitEvent event)
        {
            Player player = event.getPlayer();
            final User user = getExactUser(player);
            onlineUsers.remove(user);
            final int taskId = event.getPlayer().getServer().getScheduler().scheduleSyncDelayedTask(core, new Runnable() {
                @Override
                public void run()
                {
                    scheduledForRemoval.remove(user.getName());
                    user.lastseen = new Timestamp(System.currentTimeMillis());
                    storage.update(user); // is async
                    if (!user.isOnline())
                    {
                        return;
                    }
                    cachedUsers.remove(user.getName());
                }
            }, core.getConfiguration().userManagerKeepUserLoaded);

            if (taskId == -1)
            {
                core.getLogger().log(WARNING, "The delayed removed of user ''{0}'' could not be scheduled... removing him now.");
                cachedUsers.remove(user.getName());
            }

            scheduledForRemoval.put(user.getName(), taskId);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onLogin(final PlayerLoginEvent event)
        {
            if (event.getResult() == PlayerLoginEvent.Result.ALLOWED)
            {
                //onlineUsers.add();
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onJoin(final PlayerJoinEvent event)
        {
            final Player player = event.getPlayer();
            final User user = cachedUsers.get(player.getName());
            if (user != null)
            {
                user.offlinePlayer = player;
                user.refreshIP();
            }
            final int removalTask = scheduledForRemoval.get(player.getName());
            if (removalTask > -1)
            {
                player.getServer().getScheduler().cancelTask(removalTask);
            }
        }
    }

    private class UserCleanupTask implements Runnable
    {
        @Override
        public void run()
        {
            for (User user : cachedUsers.values())
            {
                if (!user.isOnline() && scheduledForRemoval.get(user.getName()) > -1) // Do not delete users that will be deleted anyway
                {
                    cachedUsers.remove(user.getName());
                }
            }
        }

    }
}
