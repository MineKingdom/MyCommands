package net.minekingdom.MyCommands.formatting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.Validate;
import org.spout.api.command.CommandArguments;
import org.spout.api.command.CommandSource;
import org.spout.api.exception.CommandException;


public class FlagFormatter implements Formatter {
    
    Map<String, Integer> flags;

    public FlagFormatter(String[] flags, int[] parameters) {
        Validate.isTrue(flags.length == parameters.length, "There must be an equal number of flags and parameter numbers");
        
        this.flags = new HashMap<String, Integer>(flags.length);
        
        for (int i = 0; i < flags.length; i++) {
            Validate.isTrue(parameters[i] > 0, "Flag parameters must be positive");
            this.flags.put(flags[i], parameters[i]);
        }
    }

    @Override
    public Map<String, Object> format(CommandSource source, AtomicReference<CommandArguments> args) throws CommandException {
        List<String> newArgs = args.get().get();
        Map<String, Object> result = new HashMap<String, Object>();
        
        boolean argsChanged = false;
        for (int i = 0; i < newArgs.size(); i++) {
            String s = newArgs.get(i);
            
            String prefix;
            String flag = null;
            int parameters = 0;
            if (s.startsWith("--") && s.length() > 2) {
                prefix = "--";
                flag = s.substring(2);
                if (!flags.containsKey(flag)) {
                    throw new CommandException("Flag \"" + prefix + flag + " does not exist.");
                }
                
                parameters = flags.get(flag);
                
                newArgs.remove(i);
            } else if (s.startsWith("-") && s.length() > 1) {
                prefix = "-";
                for (int j = 1; j < s.length(); j++) {
                    flag = new String(new char[] {s.charAt(j)});
                    if (!flags.containsKey(flag)) {
                        throw new CommandException("Flag \"" + prefix + flag + " does not exist.");
                    }
                    parameters = flags.get(flag);
                    if (parameters != 0 && j != s.length() - 1) {
                        throw new CommandException("Flag \"" + prefix + flag + " must have " + parameters + " parameters.");
                    }
                    
                    if (parameters == 0) {
                        result.put(flag, new String[0]);
                    } else if (j != s.length() - 1) {
                        throw new CommandException("Flag \"" + prefix + flag + " must have " + parameters + " parameters.");
                    }
                }
                
                newArgs.remove(i);
            } else {
                continue;
            }
            
            if (parameters != 0) {
                String[] params = new String[parameters];
                int j = 0;
                try {
                    while (parameters-- > 0) {
                        params[j++] = newArgs.get(i);
                        newArgs.remove(i);
                    }
                } catch (Exception ex) {
                    throw new CommandException("Flag \"" + prefix + flag + " must have " + parameters + " parameters.");
                }
                
                result.put(flag, params);
            }
            
            argsChanged = true;
            --i;
        }
        
        if (argsChanged) {
            args.set(new CommandArguments(newArgs));
        }
        
        return result;
    }

}
