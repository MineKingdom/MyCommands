package net.minekingdom.MyCommands;

import java.util.HashMap;
import java.util.Random;

import org.spout.api.chat.ChatArguments;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;
import org.spout.api.command.annotated.Command;
import org.spout.api.command.annotated.CommandPermissions;
import org.spout.api.entity.Player;
import org.spout.api.exception.CommandException;
import org.spout.api.geo.discrete.Point;

public class Environment {
    
    @SuppressWarnings("unused")
    private final MyCommands plugin;
    
    public Environment(MyCommands plugin)
    {
        this.plugin = plugin;
    }
    
    @Command(aliases = {"set", "="}, desc = "Sets an environment variable", min = 2, max = 2, usage = "<variable> <value>")
    @CommandPermissions("mycommands.set")
    public void set(CommandContext args, CommandSource source) throws CommandException
    {
        Environment.variables.put(args.get(0).getPlainString(), args.get(1).getPlainString());
    }
    
    private static HashMap<String, String> variables;
    
    protected static void init()
    {
        variables = new HashMap<String, String>();
    }
    
    public static ChatArguments processMessage(CommandSource source, ChatArguments msg)
    {
        String s = msg.toFormatString();
        char[] str = s.toCharArray();
        
        for ( int i = 0; i < str.length; i++ )
        {
            if ( str[i] == '$' )
            {
                String var;
                {
                    int size = 0;
                    while ( (i + ++size) < str.length && isGoodFormat(str[i + size]) );
                    char[] buffer = new char[--size];
                    
                    for ( int j = 0; j < size; j++ )
                    {
                        buffer[j] = str[i + j + 1];
                    }
                    
                    var = new String(buffer);
                    i += size;
                }
                
                if ( var.equals("a") )
                    continue;
                
                if ( source.hasPermission("mycommands.environment." + var) 
                  || source.hasPermission("mycommands.environment.*") )
                {
                    String repl = getVariableAs(source, var);
                    if ( repl != null )
                        s = s.replaceAll("\\$" + var, repl);
                }
            }
        }
        
        return ChatArguments.fromFormatString(s);
    }
    
    private static boolean isGoodFormat(char c)
    {
        return Character.isLetter(c) || Character.isDigit(c) || c == '-' || c == '_';
    }

    public static String getVariableAs(CommandSource source, String name)
    {
        if ( name.equals("r") )
        {
            Player[] players = MyCommands.getInstance().getServer().getOnlinePlayers();
            if ( players.length != 0 )
            {
                Random rand = new Random();
                return players[rand.nextInt(players.length)].getName();
            }
            return null;
        }
        
        if ( name.equals("me") )
        {
            return source.getName();
        }
        
        if ( name.equals("pos") )
        {
            if ( source instanceof Player )
            {
                Point p = ((Player) source).getTransform().getPosition();
                return "(" + p.getWorld().getName() + ":" + p.getX() + "," + p.getY() + "," + p.getZ() + ")";
            }
            return null;
        }
        
        if ( name.equals("world") )
        {
            if ( source instanceof Player )
            {
                return ((Player) source).getWorld().getName();
            }
            return null;
        }
        
        if ( name.equals("world-spawn") )
        {
            if ( source instanceof Player )
            {
                Point p = ((Player) source).getWorld().getSpawnPoint().getPosition();
                return "(" + p.getWorld().getName() + ":" + p.getX() + "," + p.getY() + "," + p.getZ() + ")";
            }
            return null;
        }
        
        return variables.get(name);
    }

    public static void flush()
    {
        variables.clear();
    }

}
