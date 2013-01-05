package net.minekingdom.MyCommands;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minekingdom.MyCommands.annotated.CommandLoadOrder;
import net.minekingdom.MyCommands.annotated.MyAnnotatedCommandRegistrationFactory;
import net.minekingdom.MyCommands.annotated.CommandLoadOrder.Order;
import net.minekingdom.MyCommands.config.PluginConfig;

import org.spout.api.Server;
import org.spout.api.UnsafeMethod;
import org.spout.api.chat.ChatArguments;
import org.spout.api.command.CommandRegistrationsFactory;
import org.spout.api.command.CommandSource;
import org.spout.api.command.annotated.Command;
import org.spout.api.command.annotated.SimpleAnnotatedCommandExecutorFactory;
import org.spout.api.command.annotated.SimpleInjector;
import org.spout.api.event.Listener;
import org.spout.api.exception.ConfigurationException;
import org.spout.api.plugin.CommonPlugin;
import org.spout.api.plugin.Plugin;

public class MyCommands extends CommonPlugin {

    private static MyCommands instance;
    private static Logger logger;
    
    private Server server;
    
    private File componentFolder;
    private File configFolder;
    
    private PluginConfig config;
    private ConfigurationManager configurationManager;

    @Override
    @UnsafeMethod
    public void onEnable()
    {
        instance = this;
        logger = getLogger();
        
        server = (Server) this.getEngine();
        
        try
        {
            config = new PluginConfig(getDataFolder());
            config.load();
        }
        catch (ConfigurationException e)
        {
            log(Level.SEVERE, "There is an error in the configuration file. Please fix the error or delete the file to regenerate a new one.");
            log(Level.SEVERE, e.getMessage());
            getPluginLoader().disablePlugin(this);
            return;
        }
        
        configurationManager = new ConfigurationManager();
        
        Environment.init();
        
        this.componentFolder = new File(getDataFolder() + File.separator + "components");
        if ( !this.componentFolder.exists() )
            this.componentFolder.mkdirs();
        
        this.configFolder = new File(getDataFolder() + File.separator + "config");
        if ( !this.configFolder.exists() )
            this.configFolder.mkdirs();

        this.getEngine().getEventManager().registerEvents(new CoreListener(), this);
        
        loadComponents();
        
        log("MyCommands v" + this.getDescription().getVersion() + " enabled.");
    }
    
    @SuppressWarnings({ "rawtypes" })
    private void loadComponents()
    {
        List<File> files = new ArrayList<File>();
        addTree(this.componentFolder, files);
        
        List<Class<?>> components = new LinkedList<Class<?>>();
        
        List<String> classes = new ArrayList<String>();
        List<URL> urls = new ArrayList<URL>();
        
        // Adds the url of every jar file.
        {
            try { urls.add(this.componentFolder.toURI().toURL()); }
            catch ( MalformedURLException ex ) {}
            
            for ( File file : files )
            {
                if ( file.getName().endsWith(".jar") )
                {
                    try 
                    { 
                        urls.add(new URL("jar:" + file.toURI().toURL() + "!/")); 
                        classes.add(file.getName().substring(0, file.getName().length() - 4));
                    }
                    catch ( MalformedURLException ex ) {}
                    continue;
                }
                
                classes.add(file.getName().substring(0, file.getName().length() - 6));
            }
        }
        
        URLClassLoader ucl = new URLClassLoader(urls.toArray(new URL[urls.size()]), this.getClassLoader());
        
        // loads every class and puts them into the components list
        {
            int normalIndex = 0;
            for ( String name : classes )
            {
                try
                {
                    Class<?> c = Class.forName(name, true, ucl);
                    
                    CommandLoadOrder loadOrder = c.getAnnotation(CommandLoadOrder.class);
                    if ( loadOrder != null )
                    {
                        if ( loadOrder.value().equals(Order.FIRST) )
                        {
                            normalIndex++;
                            components.add(0, c);
                            continue;
                        }
                        else if ( loadOrder.value().equals(Order.LAST) )
                        {
                            components.add(c);
                            continue;
                        }
                    }
                        
                    components.add(normalIndex, c);
                }
                catch ( ClassNotFoundException ex ) {}
            }
        }
        
        // Registers the commands and the listeners
        {
            CommandRegistrationsFactory<Class<?>> commandRegFactory = new MyAnnotatedCommandRegistrationFactory(new SimpleInjector(this), new SimpleAnnotatedCommandExecutorFactory());
            
            this.getEngine().getRootCommand().addSubCommands(this, Environment.class, commandRegFactory);
            
            for ( Class<?> c : components )
            {
                if ( Listener.class.isAssignableFrom(c) )
                {
                    try
                    {
                        Constructor ctor = c.getDeclaredConstructor(Plugin.class);
                        ctor.setAccessible(true);
                        Listener l = (Listener) ctor.newInstance(this);
                        this.getEngine().getEventManager().registerEvents(l, this);
                    }
                    catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
                    {
                        e.printStackTrace();
                    }
                }
                
                if ( PluginConfig.REPLACE_COMMANDS.getBoolean() )
                {
                    for ( Method m : c.getMethods() )
                    {
                        Command annotation = m.getAnnotation(Command.class);
                        if ( annotation == null )
                            continue;
                        
                        for ( String name : annotation.aliases() )
                            this.getEngine().getRootCommand().removeChild(name);
                    }
                }
                
                try
                {
                    this.getEngine().getRootCommand().addSubCommands(this, c, commandRegFactory);
                    log(c.getName() + " component sucessfully loaded.");
                }
                catch (Throwable t)
                {
                    log(Level.SEVERE, c.getName() + " failed to load.\n");
                    t.printStackTrace();
                }
            }
        }
        
        try
        {
            ucl.close();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    private void addTree(File file, List<File> files)
    {
        File[] children = file.listFiles();
        if ( children != null )
        {
            for ( File child : children )
            {
                if ( (child.getName().endsWith(".class") || child.getName().endsWith(".jar")) )
                    files.add(child);
                
                addTree(child, files);
            }
        }
    }
    
    @Override
    public void onReload()
    {
        try
        {
            config.load();
        }
        catch (ConfigurationException e)
        {
            log(Level.SEVERE, "There is an error in the configuration file. Please fix the error or delete the file to regenerate a new one.");
            log(Level.SEVERE, e.getMessage());
            getPluginLoader().disablePlugin(this);
            return;
        }
        
        this.configurationManager.flush();
        
        Environment.load();
        loadComponents();
    }
    
    @Override
    @UnsafeMethod
    public void onDisable()
    {
        this.configurationManager.save();
        log("MyCommands v" + this.getDescription().getVersion() + " disabled.");
    }
    
    public static void log(String msg)
    {
        log(Level.INFO, msg);
    }
    
    public static void log(Level level, String msg)
    {
        logger.log(level, msg);
    }

    public static MyCommands getInstance()
    {
        return instance;
    }
    
    public Server getServer()
    {
        return server;
    }

    public static void sendMessage(CommandSource source, Object... objects)
    {
        source.sendMessage(new ChatArguments().append(objects));
    }

    public ConfigurationManager getConfigurationManager()
    {
        return this.configurationManager;
    }

    public File getConfigFolder()
    {
        return this.configFolder;
    }
    
}