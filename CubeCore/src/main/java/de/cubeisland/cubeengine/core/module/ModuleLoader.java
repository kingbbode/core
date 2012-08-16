package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.util.Validate;
import de.cubeisland.cubeengine.core.util.log.CubeLogger;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author Phillip Schichtel
 */
public class ModuleLoader
{
    private final LibraryClassLoader libClassLoader;
    private final Map<String, ModuleClassLoader> classLoaders;
    private final Core core;
    public static final String CLASS_PREFIX = "Cube";
    private static final String BASE_FQDN = "de.cubeisland.cubeengine.";
    private static final String INFO_FILE = "module.yml";

    public ModuleLoader(Core core)
    {
        this.libClassLoader = new LibraryClassLoader(this.getClass().getClassLoader());
        this.classLoaders = new HashMap<String, ModuleClassLoader>();
        this.core = core;
    }

    public Module loadModule(File file) throws InvalidModuleException, MissingDependencyException
    {
        return this.loadModule(this.loadModuleInfo(file));
    }

    public Module loadModule(ModuleInfo info) throws InvalidModuleException, MissingDependencyException
    {
        try
        {
            final String name = info.getName();

            for (String dep : info.getDependencies())
            {
                if (!this.classLoaders.containsKey(dep))
                {
                    throw new MissingDependencyException(dep);
                }
            }

            ModuleClassLoader classLoader = new ModuleClassLoader(this, info, this.core.getClass().getClassLoader());
            Module module = Class.forName(BASE_FQDN + name.toLowerCase(Locale.ENGLISH) + "." + CLASS_PREFIX + name, true, classLoader).asSubclass(Module.class).getConstructor().newInstance();
            module.initialize(this.core, info, new PluginWrapper(this.core, module), new CubeLogger(name), new File(info.getFile().getParentFile(), name), classLoader);
            this.classLoaders.put(name, classLoader);
            return module;
        }
        catch (Exception e)
        {
            if (e instanceof MissingDependencyException)
            {
                throw (MissingDependencyException)e;
            }
            throw new InvalidModuleException("Module: " + info.getName(), e);
        }
    }

    public void unloadModule(Module module)
    {
        Validate.notNull(module, "The module must not be null!");
    }

    public ModuleInfo loadModuleInfo(File file) throws InvalidModuleException
    {
        Validate.fileExists(file, "The file must exist!");
        if (!FileExtentionFilter.JAR.accept(file))
        {
            throw new IllegalArgumentException("The file doesn't seem");
        }
        ModuleInfo info;
        JarFile jarFile = null;
        try
        {
            jarFile = new JarFile(file);

            JarEntry entry = jarFile.getJarEntry(INFO_FILE);
            if (entry == null)
            {
                throw new InvalidModuleException("The module '" + file.getPath() + "' does not contain a module.yml!");
            }
            InputStream configStream = jarFile.getInputStream(entry);
            info = new ModuleInfo(file, Configuration.load(ModuleConfiguration.class, configStream));
        }
        catch (IOException e)
        {
            throw new InvalidModuleException("File: " + file.getPath(), e);
        }
        finally
        {
            if (jarFile != null)
            {
                try
                {
                    jarFile.close();
                }
                catch (IOException e)
                {}
            }
        }
        return info;
    }

    public Class<?> getClazz(ModuleInfo info, String name)
    {
        if (name == null)
        {
            return null;
        }
        Class<?> clazz = null;
        try
        {
            clazz = this.libClassLoader.findClass(name);
        }
        catch (ClassNotFoundException e)
        {}

        if (clazz != null)
        {
            return clazz;
        }

        Set<String> alreadyChecked = new HashSet<String>(this.classLoaders.size() / 2);

        for (String dep : info.getSoftDependencies())
        {
            try
            {                                     //TODO STACKOVERFLOW
                clazz = this.classLoaders.get(dep).findClass(name);
                if (clazz != null)
                {
                    return clazz;
                }
            }
            catch (ClassNotFoundException e)
            {}
        }

        for (String dep : info.getDependencies())
        {
            if (alreadyChecked.contains(dep))
            {
                continue;
            }
            else
            {
                alreadyChecked.add(dep);
            }
            try
            {
                clazz = this.classLoaders.get(dep).findClass(name);
                if (clazz != null)
                {
                    return clazz;
                }
            }
            catch (ClassNotFoundException e)
            {}
        }

        for (String module : this.classLoaders.keySet())
        {
            if (!alreadyChecked.contains(module))
            {
                try
                {
                    clazz = this.classLoaders.get(module).findClass(name);
                    if (clazz != null)
                    {
                        return clazz;
                    }
                }
                catch (ClassNotFoundException e)
                {}
            }
        }

        return null;
    }

    public LibraryClassLoader getLibraryClassLoader()
    {
        return this.libClassLoader;
    }
}