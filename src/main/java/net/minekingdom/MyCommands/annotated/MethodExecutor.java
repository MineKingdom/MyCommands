package net.minekingdom.MyCommands.annotated;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import net.minekingdom.MyCommands.MyCommands;
import net.minekingdom.MyCommands.formatting.FlagFormatter;
import net.minekingdom.MyCommands.formatting.Formatter;

import org.apache.commons.lang3.Validate;
import org.spout.api.command.Command;
import org.spout.api.command.CommandArguments;
import org.spout.api.command.CommandSource;
import org.spout.api.command.Executor;
import org.spout.api.exception.CommandException;
import org.spout.api.exception.WrappedCommandException;

public class MethodExecutor implements Executor {

    private Object instance;
    private Method method;
    private Formatter[] formatters;
    private Formatter flagFormatter;

    public MethodExecutor(Object instance, Method method) {
        this.instance = instance;
        this.method = method;
        
        Validate.notNull(method);
        
        Class<?>[] types = method.getParameterTypes();
        Validate.isTrue(types.length == 2 && types[0].equals(CommandSource.class) && types[1].equals(CommandArguments.class));
        
        CommandFlags commandFlags = this.method.getAnnotation(CommandFlags.class);
        if (commandFlags != null) {
            this.flagFormatter = new FlagFormatter(commandFlags.value(), commandFlags.parameters());
        }
        
        CommandFormat commandFormat = this.method.getAnnotation(CommandFormat.class);
        if (commandFormat == null) {
            formatters = new Formatter[0];
        } else {
            Class<? extends Formatter>[] classes = commandFormat.value();
            formatters = new Formatter[classes.length];
            
            int i = 0;
            for (Class<? extends Formatter> clazz : classes) {
                try {
                    Constructor<? extends Formatter> ctor = clazz.getConstructor();
                    ctor.setAccessible(true);
                    formatters[i++] = ctor.newInstance();
                } catch (Exception e) {
                    MyCommands.log(Level.WARNING, "Could not instanciate formatter \"" + clazz.getName() + "\", as it does not contain a empty constructor.");
                }
            }
        }
    }
    
    @Override
    public void execute(CommandSource source, Command command, CommandArguments args) throws CommandException {
        method.setAccessible(true);
        try {
            AtomicReference<CommandArguments> ref = new AtomicReference<CommandArguments>(args);
            
            Map<String, Object> flags = flagFormatter == null ? new HashMap<String, Object>() : flagFormatter.format(source, ref);
            
            Map<String, Object> map = new HashMap<String, Object>();
            for (Formatter f : formatters) {
                try {
                    map.putAll(f.format(source, ref));
                } catch (Throwable t) {
                    MyCommands.log(Level.WARNING, "An exception occured in the formatting of the command \"" + command.getName() + "\" by the formatter " + f.getClass().getName() + ". Unexpected behavior may occur.");
                    t.printStackTrace();
                }
            }
            
            Class<?>[] types = method.getParameterTypes();
            Object[] parameters = new Object[types.length];
            for (int i = 0; i < parameters.length; i++) {
                if (types[i].equals(CommandSource.class)) {
                    parameters[i] = source;
                } else if (types[i].equals(CommandArguments.class)) {
                    parameters[i] = ref.get();
                } else if (types[i].equals(Command.class)) {
                    parameters[i] = command;
                } else {
                    Annotation[][] annotations = method.getParameterAnnotations();
                    for (int j = 0; j < annotations[i].length; j++) {
                        if (annotations[i][j] instanceof Arg) {
                            parameters[i] = map.get(((Arg) annotations[i][j]).value());
                            break;
                        } else if (annotations[i][j] instanceof Flag) {
                            parameters[i] = flags.get(((Flag) annotations[i][j]).value());
                            break;
                        }
                    }
                }
            }
            method.invoke(instance, parameters);
        } catch (IllegalAccessException e) {
            throw new WrappedCommandException(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            throw cause instanceof CommandException ? (CommandException) cause : new WrappedCommandException(e);
        }
    }
}
