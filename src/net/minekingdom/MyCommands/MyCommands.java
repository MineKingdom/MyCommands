package net.minekingdom.MyCommands;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.spout.api.Server;
import org.spout.api.UnsafeMethod;
import org.spout.api.chat.ChatArguments;
import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.CommandRegistrationsFactory;
import org.spout.api.command.CommandSource;
import org.spout.api.command.annotated.AnnotatedCommandRegistrationFactory;
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
    
    private PluginConfig config;

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
        
        Environment.init();
        
        this.componentFolder = new File(getDataFolder() + File.separator + "components");
        if ( !this.componentFolder.exists() )
            this.componentFolder.mkdirs();

        this.getEngine().getEventManager().registerEvents(new CoreListener(), this);
        
        loadComponents();
        
        log("MyCommands v" + this.getDescription().getVersion() + " enabled.");
    }
    
    @SuppressWarnings({ "rawtypes" })
    private void loadComponents()
    {
        List<File> files = new ArrayList<File>();
        addTree(this.componentFolder, files);
        
        try
        {
            URL[] classes = { this.componentFolder.toURI().toURL() };
            URLClassLoader ucl = new URLClassLoader(classes, this.getClassLoader());
            
            CommandRegistrationsFactory<Class<?>> commandRegFactory = new AnnotatedCommandRegistrationFactory(new SimpleInjector(this), new SimpleAnnotatedCommandExecutorFactory());
            
            this.getEngine().getRootCommand().addSubCommands(this, Environment.class, commandRegFactory);
            
            for ( File file : files )
            {
                Class<?> c = ucl.loadClass(file.getName().replace(".class", ""));
                
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
                        for ( Annotation a : m.getAnnotations() )
                        {
                            if ( a instanceof Command )
                            {
                                for ( String name : ((Command) a).aliases() )
                                {
                                    this.getEngine().getRootCommand().removeChild(name);
                                }
                            }
                        }
                    }
                }
                
                this.getEngine().getRootCommand().addSubCommands(this, c, commandRegFactory);
            }
            
            ucl.close();
        }
        catch ( ClassNotFoundException | IOException e)
        {
            e.printStackTrace();
        }
    }
    
    private void addTree(File file, List<File> files)
    {
        File[] children = file.listFiles();
        if ( children != null )
        {
            for ( File child : children )
            {
                if ( child.getName().endsWith(".class") )
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
        
        Environment.load();
        loadComponents();
    }
    
    @Override
    @UnsafeMethod
    public void onDisable()
    {
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

    public static void sendErrorMessage(CommandSource source, Object... objects)
    {
        source.sendMessage(new ChatArguments().append(ChatStyle.DARK_RED, "[MyCommands] ", ChatStyle.RED, objects));
    }
    
}