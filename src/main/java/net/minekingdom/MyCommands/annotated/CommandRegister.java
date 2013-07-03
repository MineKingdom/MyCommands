package net.minekingdom.MyCommands.annotated;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import net.minekingdom.MyCommands.MyCommands;

import org.spout.api.Client;
import org.spout.api.Spout;
import org.spout.api.command.annotated.Binding;
import org.spout.api.command.annotated.Command;
import org.spout.api.command.annotated.Filter;
import org.spout.api.command.annotated.Permissible;
import org.spout.api.command.annotated.Platform;
import org.spout.api.command.filter.CommandFilter;
import org.spout.api.event.Listener;
import org.spout.api.plugin.Plugin;
import org.spout.api.util.config.Configuration;

public final class CommandRegister {
    
    private CommandRegister() {
    }
    
    public static void register(Class<?> clazz) {
        register(clazz, null);
    }
    
    public static void register(Class<?> clazz, org.spout.api.command.Command parent) {
        Object instance;
        try {
            Constructor<?> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            instance = ctor.newInstance();
        } catch (Exception ex) {
            try {
                Constructor<?> ctor = clazz.getDeclaredConstructor(Plugin.class);
                ctor.setAccessible(true);
                instance = ctor.newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Class does not have a valid constructor.");
            }
        }
        register(clazz, instance, parent);
    }
    
    @SuppressWarnings({ "unchecked" })
    private static void register(Class<?> commands, Object instance, org.spout.api.command.Command parent) {
        Class<? extends Configuration> configClass = null;

        CommandConfiguration configAnnotation = commands.getAnnotation(CommandConfiguration.class);
        if (configAnnotation == null) {
            Class<?>[] subclasses = commands.getDeclaredClasses();
            for (Class<?> clazz : subclasses) {
                if (Configuration.class.isAssignableFrom(clazz)) {
                    configClass = (Class<? extends Configuration>) clazz;
                    break;
                }
            }
        } else {
            configClass = configAnnotation.value();
        }
        
        MyCommands plugin = MyCommands.getInstance();

        if (configClass != null) {
            try {
                Constructor<? extends Configuration> ctor = configClass.getConstructor(File.class);
                ctor.setAccessible(true);

                Configuration config = ctor.newInstance(new File(plugin.getConfigFolder() + File.separator + commands.getSimpleName() + ".yml"));

                plugin.getConfigurationManager().addConfig(commands, config);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (instance instanceof Listener) {
            plugin.getEngine().getEventManager().registerEvents((Listener) instance, plugin);
        }
        
        for (Method method : commands.getMethods()) {
            
            Command command = method.getAnnotation(Command.class);
            if (command != null) {
                Platform platform = method.getAnnotation(Platform.class);
                if (platform != null) {
                    org.spout.api.Platform[] allowed = platform.value();
                    int i;
                    for (i = 0; i < allowed.length && allowed[i] != Spout.getPlatform(); ++i);
                    if (i == allowed.length) {
                        continue;
                    }
                }
                
                org.spout.api.command.Command cmd = (parent != null ? parent.getChild(command.aliases()[0]) : plugin.getEngine().getCommandManager().getCommand(command.aliases()[0]))
                        .addAlias(command.aliases())
                        .setArgumentBounds(command.min(), command.max())
                        .setUsage(command.usage())
                        .setHelp(command.desc());
                
                Permissible permissible = method.getAnnotation(Permissible.class);
                if (permissible != null) {
                    cmd.setPermission(permissible.value());
                }
                
                Filter filter = method.getAnnotation(Filter.class);
                if (filter != null) {
                    Class<? extends org.spout.api.command.filter.CommandFilter>[] filters = filter.value();
                    for (Class<? extends org.spout.api.command.filter.CommandFilter> c : filters) {
                        try {
                            Constructor<? extends CommandFilter> ctor = c.getDeclaredConstructor();
                            ctor.setAccessible(true);
                            cmd.addFilter(ctor.newInstance());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                
                Binding binding = method.getAnnotation(Binding.class);
                if (binding != null && plugin.getEngine() instanceof Client) {
                    org.spout.api.input.Binding bind = new org.spout.api.input.Binding(cmd.getName(), binding.value(), binding.mouse()).setAsync(binding.async());
                    ((Client) plugin.getEngine()).getInputManager().bind(bind);
                }
                
                boolean hasExecutor = true;
                
                NestedCommand nested = method.getAnnotation(NestedCommand.class);
                if (nested != null) {
                    register(nested.value(), cmd);
                    if (nested.ignoreBody()) {
                        hasExecutor = false;
                    }
                }
                
                if (hasExecutor) {
                    cmd.setExecutor(new MethodExecutor(instance, method));
                }
            }
        }
    }
}