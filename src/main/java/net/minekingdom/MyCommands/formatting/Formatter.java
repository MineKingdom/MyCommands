package net.minekingdom.MyCommands.formatting;

import java.util.Map;

import org.spout.api.command.CommandArguments;
import org.spout.api.command.CommandSource;

public interface Formatter {
    public Map<String, Object> format(CommandSource source, CommandArguments args);
}
