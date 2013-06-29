package net.minekingdom.MyCommands;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minekingdom.MyCommands.annotated.CommandLoadOrder;
import net.minekingdom.MyCommands.annotated.MyAnnotatedCommandExecutorFactory;
import net.minekingdom.MyCommands.annotated.CommandLoadOrder.Order;
import net.minekingdom.MyCommands.config.PluginConfig;

import org.spout.api.UnsafeMethod;
import org.spout.api.command.CommandSource;
import org.spout.api.command.Executor;
import org.spout.api.command.annotated.Command;
import org.spout.api.exception.ConfigurationException;
import org.spout.api.plugin.Plugin;

public class MyCommands extends Plugin {

    private static MyCommands    instance;
    private static Logger        logger;

    private File                 componentFolder;
    private File                 configFolder;

    private PluginConfig         config;
    private ConfigurationManager configurationManager;

    private Set<CommandInfo>     replacedCommands;

    @Override
    @UnsafeMethod
    public void onEnable() {
        instance = this;
        logger = getLogger();

        try {
            config = new PluginConfig(getDataFolder());
            config.load();
        } catch (ConfigurationException e) {
            log(Level.SEVERE, "There is an error in the configuration file. Please fix the error or delete the file to regenerate a new one.");
            log(Level.SEVERE, e.getMessage());
            getPluginLoader().disablePlugin(this);
            return;
        }

        configurationManager = new ConfigurationManager();

        this.componentFolder = new File(getDataFolder() + File.separator + "components");
        if (!this.componentFolder.exists())
            this.componentFolder.mkdirs();

        this.configFolder = new File(getDataFolder() + File.separator + "config");
        if (!this.configFolder.exists())
            this.configFolder.mkdirs();

        this.getEngine().getEventManager().registerEvents(new CoreListener(), this);

        this.replacedCommands = new HashSet<CommandInfo>();
        loadComponents();

        log("MyCommands v" + this.getDescription().getVersion() + " enabled.");
    }

    private void loadComponents() {
        List<File> files = new ArrayList<File>();
        addTree(this.componentFolder, files);

        List<Class<?>> components = new LinkedList<Class<?>>();

        List<String> classes = new ArrayList<String>();
        List<URL> urls = new ArrayList<URL>();

        // Adds the url of every jar file.
        {
            try {
                urls.add(this.componentFolder.toURI().toURL());
            } catch (MalformedURLException ex) {}

            for (File file : files) {
                if (file.getName().endsWith(".jar")) {
                    try {
                        urls.add(new URL("jar:" + file.toURI().toURL() + "!/"));
                        classes.add(file.getName().substring(0, file.getName().length() - 4));
                    } catch (MalformedURLException ex) {}
                    continue;
                }

                classes.add(file.getName().substring(0, file.getName().length() - 6));
            }
        }

        URLClassLoader ucl = new URLClassLoader(urls.toArray(new URL[urls.size()]), this.getClassLoader());

        // loads every class and puts them into the components list
        {
            int normalIndex = 0;
            for (String name : classes) {
                try {
                    Class<?> c = Class.forName(name, true, ucl);

                    CommandLoadOrder loadOrder = c.getAnnotation(CommandLoadOrder.class);
                    if (loadOrder != null) {
                        if (loadOrder.value().equals(Order.FIRST)) {
                            normalIndex++;
                            components.add(0, c);
                            continue;
                        } else if (loadOrder.value().equals(Order.LAST)) {
                            components.add(c);
                            continue;
                        }
                    }

                    components.add(normalIndex, c);
                } catch (ClassNotFoundException ex) {}
            }
        }

        // Registers the commands and the listeners
        {
            for (Class<?> c : components) {
                if (PluginConfig.REPLACE_COMMANDS.getBoolean()) {
                    for (Method m : c.getMethods()) {
                        Command annotation = m.getAnnotation(Command.class);
                        if (annotation == null)
                            continue;

                        /*for (String name : annotation.aliases()) {
                            this.getEngine().getCommandManager().getRootCommand().removeChild(name);
                        }*/
                    }
                }

                try {
                    MyAnnotatedCommandExecutorFactory.create(c);
                    log(c.getName() + " component sucessfully loaded.");
                } catch (Throwable t) {
                    log(Level.SEVERE, c.getName() + " failed to load.\n");
                    t.printStackTrace();
                }
            }
        }

        try {
            ucl.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void addTree(File file, List<File> files) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                if ((child.getName().endsWith(".class") || child.getName().endsWith(".jar")))
                    files.add(child);

                addTree(child, files);
            }
        }
    }

    public void restoreReplacedCommands() {
        for (CommandInfo cmd : this.replacedCommands) {
            this.getEngine().getCommandManager().getCommand(cmd.getPreferredName(), false)
                    .setExecutor(cmd.getExecutor())
                    .addAlias(cmd.getAliases())
                    .setArgumentBounds(cmd.getMinArguments(), cmd.getMaxArguments())
                    .setHelp(cmd.getHelp())
                    .setPermission(cmd.getPemission())
                    .setUsage(cmd.getUsage());
        }
    }

    @Override
    public void onReload() {
        try {
            config.load();
        } catch (ConfigurationException e) {
            log(Level.SEVERE, "There is an error in the configuration file. Please fix the error or delete the file to regenerate a new one.");
            log(Level.SEVERE, e.getMessage());
            getPluginLoader().disablePlugin(this);
            return;
        }

        if (!PluginConfig.REPLACE_COMMANDS.getBoolean() && !this.replacedCommands.isEmpty()) {
            restoreReplacedCommands();
        }
        this.configurationManager.flush();

        loadComponents();
    }

    @Override
    @UnsafeMethod
    public void onDisable() {
        this.configurationManager.save();
        log("MyCommands v" + this.getDescription().getVersion() + " disabled.");
    }

    public static void log(String msg) {
        log(Level.INFO, msg);
    }

    public static void log(Level level, String msg) {
        logger.log(level, msg);
    }

    public static MyCommands getInstance() {
        return instance;
    }

    public static void sendMessage(CommandSource source, String message) {
        source.sendMessage(message);
    }

    public ConfigurationManager getConfigurationManager() {
        return this.configurationManager;
    }

    public File getConfigFolder() {
        return this.configFolder;
    }

    public class CommandInfo {

        private Executor        executor;
        private List<String>    aliases;
        private String          help;
        private String          usage;
        private String          permission;
        private int             min;
        private int             max;

        public CommandInfo(org.spout.api.command.Command command) {
            this.help       = command.getHelp();
            this.aliases    = command.getAliases();
            this.usage      = command.getUsage();
            this.permission = command.getPermission();
            this.executor   = command.getExecutor();
            this.min        = command.getMinArguments();
            this.max        = command.getMaxArguments();
        }

        public String getPemission() {
            return this.permission;
        }

        public String getUsage() {
            return this.usage;
        }

        public String getHelp() {
            return this.help;
        }

        public int getMaxArguments() {
            return this.max;
        }

        public int getMinArguments() {
            return this.min;
        }

        public String[] getAliases() {
            String[] out = new String[this.aliases.size()];
            this.aliases.toArray(out);
            return out;
        }

        public Executor getExecutor() {
            return this.executor;
        }

        public String getPreferredName() {
            return aliases.get(0);
        }
    }

}