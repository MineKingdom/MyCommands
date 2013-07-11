package net.minekingdom.MyCommands.formatting;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.spout.api.command.CommandArguments;
import org.spout.api.command.CommandSource;
import org.spout.api.exception.CommandException;

public interface Formatter {
    public Map<String, Object> format(CommandSource source, AtomicReference<CommandArguments> args) throws CommandException;
}
