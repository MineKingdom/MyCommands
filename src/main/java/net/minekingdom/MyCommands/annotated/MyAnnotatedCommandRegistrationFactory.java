package net.minekingdom.MyCommands.annotated;

import java.io.File;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;

import net.minekingdom.MyCommands.MyCommands;

import org.spout.api.Spout;
import org.spout.api.command.annotated.AnnotatedCommandRegistrationFactory;
import org.spout.api.command.annotated.SimpleAnnotatedCommandExecutorFactory;
import org.spout.api.command.annotated.SimpleInjector;
import org.spout.api.event.Listener;
import org.spout.api.util.Named;
import org.spout.api.util.config.Configuration;

public class MyAnnotatedCommandRegistrationFactory extends AnnotatedCommandRegistrationFactory {

    private MyCommands plugin;

    public MyAnnotatedCommandRegistrationFactory(MyCommands plugin) {
        super(plugin.getEngine(), new SimpleInjector(plugin), new SimpleAnnotatedCommandExecutorFactory());
        this.plugin = plugin;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean register(Named owner, Class<?> commands, Object instance, org.spout.api.command.Command parent) {
        
        CommandPlatform platform = commands.getAnnotation(CommandPlatform.class);
        if (platform != null && !platform.value().equals(Spout.getPlatform())) {
            return false;
        }
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

        return super.register(owner, commands, instance, parent);
    }
    
    @Override
    protected org.spout.api.command.Command createCommand(Named owner, org.spout.api.command.Command parent, AnnotatedElement obj) {
        CommandPlatform platform = obj.getAnnotation(CommandPlatform.class);
        if (platform != null && !platform.value().equals(Spout.getPlatform())) {
            return null;
        }

        return super.createCommand(owner, parent, obj);
    }
}