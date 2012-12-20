package de.cubeisland.cubeengine.roles.role.config;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.RolesConfig;
import de.cubeisland.cubeengine.roles.exception.CircularRoleDepedencyException;
import de.cubeisland.cubeengine.roles.exception.RoleDependencyMissingException;
import de.cubeisland.cubeengine.roles.role.ConfigRole;
import de.cubeisland.cubeengine.roles.role.Role;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class RoleProvider
{
    private RoleMirror config;
    private THashMap<String, RoleConfig> configs = new THashMap<String, RoleConfig>();
    private THashMap<String, Role> roles = new THashMap<String, Role>();
    private boolean init = false;
    private File worldfolder = null;
    private Set<Role> defaultRoles = new HashSet<Role>();
    private Stack<String> roleStack = new Stack<String>();
    private Roles module = (Roles) CubeEngine.getModuleManager().getModule("Roles");

    public RoleProvider(RoleMirror mirrorConfig)
    {
        this.config = mirrorConfig;
    }

    /**
     * RoleProvider for a single world
     *
     * @param worldId
     */
    public RoleProvider(long worldId)
    {
        this.config = new RoleMirror(this.module, worldId);
    }

    public Iterable<RoleConfig> getConfigs()
    {
        return this.configs.values();
    }

    public void addConfig(RoleConfig config)
    {
        this.configs.put(config.roleName, config);
    }

    public void setRole(Role role)
    {
        this.roles.put(role.getName(), role);
    }

    public Role getRole(String roleName)
    {
        return this.roles.get(roleName);
    }

    public RoleConfig getConfig(String parentName)
    {
        return this.configs.get(parentName);
    }

    public TLongObjectHashMap<Pair<Boolean, Boolean>> getWorlds()
    {
        return this.config.getWorlds();
    }

    public TLongObjectHashMap<List<Role>> getRolesFor(User user, boolean reload)
    {
        TLongObjectHashMap<List<Role>> result = new TLongObjectHashMap<List<Role>>();
        TLongObjectHashMap<List<String>> rolesFromDb;
        if (reload)
        {
            rolesFromDb = module.getManager().reloadRoles(user);
        }
        else
        {
            rolesFromDb = module.getManager().loadRoles(user);
        }
        for (long worldID : rolesFromDb.keys())
        {
            Pair<Boolean, Boolean> mirrorRoleUsers = this.config.getWorlds().get(worldID);
            if (mirrorRoleUsers == null)
            {
                continue; // world is not in this provider
            }
            List<Role> roleList = new ArrayList<Role>();
            result.put(worldID, roleList);
            if (mirrorRoleUsers.getLeft() == mirrorRoleUsers.getRight()// both true -> full mirror
                    || mirrorRoleUsers.getLeft()) // roles are mirrored BUT but assigned roles are not mirrored!
            {
                for (String roleName : rolesFromDb.get(worldID))
                {
                    Role role = this.getRole(roleName);
                    if (role == null)
                    {
                        throw new IllegalStateException("Role does not exist!");
                    }
                    roleList.add(role);
                }
            }
            //else roles are not mirrored BUT assigned roles are mirrored! -> this world will have its own provider
        }
        return result;
    }

    public Set<Role> getDefaultRoles()
    {
        return this.defaultRoles;
    }

    public void init(File rolesFolder)
    {
        if (this.init)
        {
            return;
        }
        this.init = true;
        if (this.worldfolder == null)
        {
            this.worldfolder = new File(rolesFolder, this.config.mainWorld);
        }
        this.worldfolder.mkdir();
        module.getLogger().debug("Loading roles for the world " + worldfolder.getName() + ":");
        int i = 0;
        for (File configFile : worldfolder.listFiles())
        {
            if (configFile.getName().endsWith(".yml"))
            {
                ++i;
                RoleConfig config = Configuration.load(RoleConfig.class, configFile);
                this.addConfig(config);
            }
        }
        module.getLogger().debug(i + " roles loaded!");
    }

    public void reload()
    {
        this.init = false;
        this.init(null);
    }

    public void loadDefaultRoles(RolesConfig config)
    {
        List<String> dRoles = config.defaultRoles.get(this.config.mainWorld);
        if (dRoles == null || dRoles.isEmpty())
        {
            module.getLogger().log(LogLevel.WARNING, "No default-roles defined for " + this.config.mainWorld);
            return;
        }
        for (String roleName : dRoles)
        {
            Role role = this.roles.get(roleName);
            if (role == null)
            {
                module.getLogger().log(LogLevel.WARNING, "Could not find default-role " + roleName);
            }
            this.defaultRoles.add(role);
        }
    }

    public void calculateRoles(THashMap<String, Role> globalRoles)
    {
        for (RoleConfig config : this.configs.values())
        {
            Role role = this.calculateRole(config, globalRoles);
            if (role == null)
            {
                module.getLogger().log(LogLevel.WARNING, config.roleName + " could not be calculated!");
                continue;
            }
            this.roles.put(role.getName(), role);
        }
    }

    public Role calculateRole(RoleConfig config, THashMap<String, Role> globalRoles)
    {
        try
        {
            Role role = this.getRole(config.roleName);
            if (role != null)
            {
                return role;
            }
            this.roleStack.push(config.roleName);
            for (String parentName : config.parents)
            {
                try
                {
                    if (this.roleStack.contains(parentName)) // Circular Dependency?
                    {
                        throw new CircularRoleDepedencyException("Cannot load role! Circular Depenency detected in " + config.roleName + "\n" + StringUtils.implode(", ", roleStack));
                    }
                    RoleConfig parentConfig;

                    if (parentName.startsWith("g:"))
                    {
                        if (globalRoles.containsKey(parentName.substring(2)))
                        {
                            continue;
                        }
                        throw new RoleDependencyMissingException("Could not find the role " + parentName);
                    }
                    else
                    {
                        parentConfig = this.configs.get(parentName);;
                    }
                    if (parentConfig == null) // Dependency Missing?
                    {
                        throw new RoleDependencyMissingException("Could not find the role " + parentName);
                    }
                    Role parentRole = this.calculateRole(parentConfig, globalRoles); // calculate parent-role
                    if (parentRole != null)
                    {
                        this.roles.put(parentRole.getName(), parentRole);
                    }
                }
                catch (RoleDependencyMissingException ex)
                {
                    module.getLogger().log(LogLevel.WARNING, ex.getMessage());
                }
            }
            // now all parent roles should be loaded

            List<Role> parentRoles = new ArrayList<Role>();
            for (String parentName : config.parents)
            {
                try
                {
                    Role parentRole;

                    if (parentName.startsWith("g:"))
                    {
                        parentRole = globalRoles.get(parentName.substring(2));
                    }
                    else
                    {
                        parentRole = this.roles.get(parentName);
                    }
                    if (parentRole == null)
                    {
                        throw new RoleDependencyMissingException("Needed parent role " + parentName + " for " + config.roleName + " not found.");
                    }
                    parentRoles.add(parentRole); // Role was found:
                }
                catch (RoleDependencyMissingException ex)
                {
                    module.getLogger().log(LogLevel.WARNING, ex.getMessage());
                }
            }
            role = new ConfigRole(config, parentRoles, false);
            this.roleStack.pop();
            return role;
        }
        catch (CircularRoleDepedencyException ex)
        {
            module.getLogger().log(LogLevel.WARNING, ex.getMessage());
            return null;
        }
    }

    public Collection<Role> getAllRoles()
    {
        return this.roles.values();
    }

    public String getMainWorld()
    {
        return this.config.mainWorld;
    }
}
