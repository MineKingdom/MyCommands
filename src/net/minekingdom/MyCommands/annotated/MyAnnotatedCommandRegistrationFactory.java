package net.minekingdom.MyCommands.annotated;

import java.io.File;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;

import net.minekingdom.MyCommands.MyCommands;

import org.spout.api.Spout;
import org.spout.api.command.annotated.AnnotatedCommandExecutorFactory;
import org.spout.api.command.annotated.AnnotatedCommandRegistrationFactory;
import org.spout.api.command.annotated.Injector;
import org.spout.api.plugin.Platform;
import org.spout.api.util.Named;
import org.spout.api.util.config.ConfigurationHolderConfiguration;

public class MyAnnotatedCommandRegistrationFactory extends AnnotatedCommandRegistrationFactory {
    
    private MyCommands plugin;
    
    public MyAnnotatedCommandRegistrationFactory(Injector injector, AnnotatedCommandExecutorFactory executorFactory)
    {
        super(injector, executorFactory);
        this.plugin = MyCommands.getInstance();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean register(Named owner, Class<?> commands, Object instance, org.spout.api.command.Command parent)
    {
        CommandPlatform platform = commands.getAnnotation(CommandPlatform.class);
        if (platform != null) 
            if ( !platform.value().equals(Spout.getPlatform()) && !platform.value().equals(Platform.ALL) )
                return false;
        
        Class<? extends ConfigurationHolderConfiguration> configClass = null;
        
        CommandConfiguration configAnnotation = commands.getAnnotation(CommandConfiguration.class);
        if ( configAnnotation == null )
        {
            Class<?>[] subclasses = commands.getDeclaredClasses();
            for ( Class<?> clazz : subclasses )
            {
                if ( ConfigurationHolderConfiguration.class.isAssignableFrom(clazz) )
                {
                    configClass = (Class<? extends ConfigurationHolderConfiguration>) clazz;
                    break;
                }
            }
        }
        else
        {
            configClass = configAnnotation.value();
        }
        
        if ( configClass != null )
        {
            try
            {
                Constructor<? extends ConfigurationHolderConfiguration> ctor = configClass.getConstructor(File.class);
                ctor.setAccessible(true);
                
                ConfigurationHolderConfiguration config = ctor.newInstance(new File(
                        plugin.getConfigFolder() + File.separator + 
                        commands.getSimpleName() + ".yml"));
                
                plugin.getConfigurationManager().addConfig(commands, config);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return super.register(owner, commands, instance, parent);
    }
    
    @Override
    protected org.spout.api.command.Command createCommand(Named owner, org.spout.api.command.Command parent, AnnotatedElement obj)
    {
        CommandPlatform platform = obj.getAnnotation(CommandPlatform.class);
        if (platform != null) 
            if ( !platform.value().equals(Spout.getPlatform()) && !platform.value().equals(Platform.ALL) )
                return null;

        return super.createCommand(owner, parent, obj);
    }
}