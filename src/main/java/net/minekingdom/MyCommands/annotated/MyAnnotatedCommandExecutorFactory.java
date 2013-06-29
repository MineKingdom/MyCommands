package net.minekingdom.MyCommands.annotated;

import java.io.File;
import java.lang.reflect.Constructor;

import net.minekingdom.MyCommands.MyCommands;

import org.spout.api.command.annotated.AnnotatedCommandExecutor;
import org.spout.api.command.annotated.AnnotatedCommandExecutorFactory;
import org.spout.api.event.Listener;
import org.spout.api.plugin.Plugin;
import org.spout.api.util.config.Configuration;

public final class MyAnnotatedCommandExecutorFactory {
    
    private MyAnnotatedCommandExecutorFactory() {
    }
    
    public static AnnotatedCommandExecutor create(Class<?> clazz) {
        return create(clazz, null);
    }
    
    public static AnnotatedCommandExecutor create(Class<?> clazz, org.spout.api.command.Command parent) {
        try {
            Constructor<?> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            return create(clazz, ctor.newInstance(), parent);
        } catch (Exception ex) {
            try {
                Constructor<?> ctor = clazz.getDeclaredConstructor(Plugin.class);
                ctor.setAccessible(true);
                return create(clazz, ctor.newInstance(), parent);
            } catch (Exception e) {
                throw new IllegalArgumentException("Class does not have a valid constructor.");
            }
        }
    }

    @SuppressWarnings({ "unchecked" })
    public static AnnotatedCommandExecutor create(Class<?> commands, Object instance, org.spout.api.command.Command parent) {
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

        return AnnotatedCommandExecutorFactory.create(instance, parent);
    }
}