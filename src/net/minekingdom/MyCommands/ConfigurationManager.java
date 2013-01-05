package net.minekingdom.MyCommands;

import java.util.HashMap;
import java.util.Map;

import org.spout.api.exception.ConfigurationException;
import org.spout.api.util.config.ConfigurationHolderConfiguration;

public class ConfigurationManager {

    private Map<Class<?>, ConfigurationHolderConfiguration> configurations;
    
    public ConfigurationManager()
    {
        this.configurations = new HashMap<Class<?>, ConfigurationHolderConfiguration>();
    }
    
    public void addConfig(Class<?> command, ConfigurationHolderConfiguration config)
    {
        try
        {
            config.load();
            this.configurations.put(command, config);
        }
        catch (ConfigurationException e)
        {
            e.printStackTrace();
        }
    }
    
    public void save()
    {
        for ( ConfigurationHolderConfiguration config : this.configurations.values() )
        {
            try
            {
                config.save();
            }
            catch (ConfigurationException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public void flush()
    {
        this.configurations.clear();
    }
}
