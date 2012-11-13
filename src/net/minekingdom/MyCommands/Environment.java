package net.minekingdom.MyCommands;

import java.util.Random;

import net.minekingdom.MyCommands.environment.Value;
import net.minekingdom.MyCommands.environment.Variable;
import net.minekingdom.MyCommands.environment.structures.PlayerStructure;
import net.minekingdom.MyCommands.environment.structures.SimpleStructure;
import net.minekingdom.MyCommands.environment.structures.WorldStructure;

import org.spout.api.chat.ChatArguments;
import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;
import org.spout.api.command.annotated.Command;
import org.spout.api.command.annotated.CommandPermissions;
import org.spout.api.entity.Player;
import org.spout.api.exception.CommandException;
import org.spout.api.geo.World;

public class Environment {
    
    @SuppressWarnings("unused")
    private final MyCommands plugin;
    
    public Environment(MyCommands plugin)
    {
        this.plugin = plugin;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Command(aliases = {"set", "="}, desc = "Sets an environment variable", min = 2, usage = "<variable> <value>")
    @CommandPermissions("mycommands.set")
    public void set(CommandContext args, CommandSource source) throws CommandException
    {
        String name = args.get(0).getPlainString();
        String value = args.getJoinedString(1).getPlainString();
        
        Variable v = Environment.variables.getVariable(name);
        if ( v == null )
        {
            Environment.variables.addVariable(new Value<String>(name, value, true));
            MyCommands.sendMessage(source, ChatStyle.CYAN, "$" + name + " has been assigned to: \"" + value + "\".");
            return;
        }
        else if ( v instanceof Value )
        {
            if ( ((Value) v).getType().equals(String.class) )
            {
                ((Value<String>) v).setValue(value);
                MyCommands.sendMessage(source, ChatStyle.CYAN, "$" + name + " has been assigned to: \"" + value + "\".");
                return;
            }
        }
        
        MyCommands.sendErrorMessage(source, "Error: variable $" + name + " is a protected variable.");
    }
    
    private static SimpleStructure variables;
    
    protected static void init()
    {
        variables = new SimpleStructure("");
        
        SimpleStructure worlds = new SimpleStructure("worlds");
        SimpleStructure players = new SimpleStructure("players");
        
        variables.addVariable(worlds);
        variables.addVariable(players);
    }
    
    protected static void load()
    {
        variables = new SimpleStructure("");
        
        SimpleStructure worlds = new SimpleStructure("worlds");
        for ( World w : MyCommands.getInstance().getServer().getWorlds())
        {
            worlds.addVariable(new WorldStructure(w.getName(), w));
        }
        
        SimpleStructure players = new SimpleStructure("players");
        for ( Player p : MyCommands.getInstance().getServer().getOnlinePlayers())
        {
            players.addVariable(new PlayerStructure(p.getName(), p));
        }
        
        variables.addVariable(worlds);
        variables.addVariable(players);
    }
    
    public static ChatArguments processMessage(CommandSource source, ChatArguments msg)
    {
        String s = msg.toFormatString() + " ";
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
                    String repl = getStringValueAs(source, var);
                    if ( repl != null )
                        s = s.replaceAll("\\$" + var + "([^a-zA-Z0-9\\-_\\.])", repl + "$1");
                }
            }
        }
        
        return ChatArguments.fromFormatString(s.substring(0, s.length() - 1));
    }
    
    private static boolean isGoodFormat(char c)
    {
        return Character.isLetter(c) || Character.isDigit(c) || c == '-' || c == '_' || c == '.';
    }

    public static String getStringValueAs(CommandSource source, String name)
    {
        Variable var;
        
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
        
        if ( name.startsWith("me") )
        {
            if ( !(source instanceof Player) )
                return source.getName();
            
            if ( name.length() == 2 )
                name = "players." + source.getName();
            else if ( name.charAt(2) == '.')
                name = "players." + source.getName() + name.substring(2);
        }
        
        var = getVariable(name);
        
        return var == null ? null : var.getStringValue();
    }

    public static Variable getVariable(String name)
    {
        return variables.getVariable(name);
    }

}
