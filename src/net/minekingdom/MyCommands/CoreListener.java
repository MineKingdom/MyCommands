package net.minekingdom.MyCommands;

import java.util.LinkedList;

import org.spout.api.chat.ChatArguments;
import org.spout.api.command.CommandSource;
import org.spout.api.entity.Player;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.Order;
import org.spout.api.event.player.PlayerJoinEvent;
import org.spout.api.event.player.PlayerLeaveEvent;
import org.spout.api.event.server.PreCommandEvent;
import org.spout.api.event.server.ServerStartEvent;
import org.spout.api.event.world.WorldLoadEvent;
import org.spout.api.event.world.WorldUnloadEvent;

import net.minekingdom.MyCommands.environment.*;
import net.minekingdom.MyCommands.environment.structures.*;

public class CoreListener implements Listener {
    
    @EventHandler
    public void onServerLoaded(ServerStartEvent event)
    {
        Environment.load();
    }
    
    @EventHandler(order = Order.EARLIEST)
    public void onWorldLoad(WorldLoadEvent event)
    {
        ((Structure) Environment.getVariable("worlds")).addVariable(new WorldStructure(event.getWorld().getName(), event.getWorld()));
    }
    
    @EventHandler(order = Order.LATEST)
    public void onWorldUnload(WorldUnloadEvent event)
    {
        ((Structure) Environment.getVariable("worlds")).removeVariable(event.getWorld().getName());
    }
    
    @EventHandler(order = Order.EARLIEST)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        ((Structure) Environment.getVariable("players")).addVariable(new PlayerStructure(event.getPlayer().getName(), event.getPlayer()));
    }
    
    @EventHandler(order = Order.LATEST)
    public void onPlayerLeave(PlayerLeaveEvent event)
    {
        ((Structure) Environment.getVariable("players")).removeVariable(event.getPlayer().getName());
    }
    
    @EventHandler(order = Order.EARLIEST)
    public void onCommand(PreCommandEvent event)
    {
        final CommandSource source = event.getCommandSource();
        
        // Replacing environment variables
        ChatArguments ca = Environment.processMessage(event.getCommandSource(), event.getArguments());
        event.setArguments(ca);
        
        String args = ca.toFormatString();
        
        // Handling the command split function
        LinkedList<String> commands = new LinkedList<String>();
        {
            char[] command = ("/" + event.getCommand() + " " + args).toCharArray();
            int split = -1;
            for ( int i = 0; i < command.length; i++ )
            {
                if ( command[i] == '|' )
                {
                    if ( i > 0 )
                        if ( command[i-1] == '\\' )
                            continue;
                    char[] buffer = new char[i - split - 1];
                    System.arraycopy(command, split + 1, buffer, 0, buffer.length);
                    commands.add(new String(buffer));
                    split = i;
                }
            }
            char[] buffer = new char[command.length - split - 1];
            System.arraycopy(command, split + 1, buffer, 0, buffer.length);
            commands.add(new String(buffer));
        }
        
        if ( commands.size() > 1 )
        {
            for ( String cmd : commands )
            {
                if ( !cmd.startsWith("/") )
                    cmd = "say " + cmd;
                else
                    cmd = cmd.substring(1);

                int space = cmd.indexOf(' ');
                if ( space == -1 )
                    event.getCommandSource().processCommand(cmd, ChatArguments.fromString(""));
                else
                    event.getCommandSource().processCommand(cmd.substring(0, space), ChatArguments.fromFormatString(cmd.substring(space + 1)));
            }
            
            event.setCancelled(true);
            return;
        }
        
        if ( args.contains("$a ") || args.endsWith("$a") )
        {
            if ( source.hasPermission("mycommands.environment.a") 
              || source.hasPermission("mycommands.environment.*") )
            {
                for ( Player player : MyCommands.getInstance().getServer().getOnlinePlayers() )
                {
                    String newArgs = args.replaceAll("\\$a", player.getName());
                    event.getCommandSource().processCommand(event.getCommand(), ChatArguments.fromFormatString(newArgs));
                }
                event.setCancelled(true);
                return;
            }
        }
    }

}
