package net.minekingdom.MyCommands;

import java.util.HashMap;
import java.util.Map;

import org.spout.cereal.config.Configuration;
import org.spout.cereal.config.ConfigurationException;

public class ConfigurationManager {

    private Map<Class<?>, Configuration> configurations;

    public ConfigurationManager() {
        this.configurations = new HashMap<Class<?>, Configuration>();
    }

    public void addConfig(Class<?> command, Configuration config) {
        try {
            config.load();
            this.configurations.put(command, config);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        for (Configuration config : this.configurations.values()) {
            try {
                config.save();
            } catch (ConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    public void flush() {
        this.configurations.clear();
    }
}
