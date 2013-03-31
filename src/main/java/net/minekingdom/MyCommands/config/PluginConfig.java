package net.minekingdom.MyCommands.config;

import java.io.File;

import org.spout.api.exception.ConfigurationException;
import org.spout.api.util.config.ConfigurationHolder;
import org.spout.api.util.config.ConfigurationHolderConfiguration;
import org.spout.api.util.config.yaml.YamlConfiguration;

public class PluginConfig extends ConfigurationHolderConfiguration {

    // General
    public static final ConfigurationHolder REPLACE_COMMANDS = new ConfigurationHolder(true, "config", "replace-existing-commands");

    public PluginConfig(File datafolder) {
        super(new YamlConfiguration(new File(datafolder + File.separator + "config.yml")));
    }

    @Override
    public void load() throws ConfigurationException {
        super.load();
        super.save();
    }

    public void save() throws ConfigurationException {
        super.save();
    }

}
