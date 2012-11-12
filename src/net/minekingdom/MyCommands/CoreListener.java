package net.minekingdom.MyCommands;

import org.spout.api.chat.ChatArguments;
import org.spout.api.command.CommandSource;
import org.spout.api.entity.Player;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.Order;
import org.spout.api.event.server.PreCommandEvent;

public class CoreListener implements Listener {
    
    @EventHandler(order = Order.EARLIEST)
    public void onCommand(PreCommandEvent event)
    {
        final CommandSource source = event.getCommandSource();
        
        // Replacing environment variables
        ChatArguments ca = Environment.processMessage(event.getCommandSource(), event.getArguments());
        event.setArguments(ca);
        
        String args = ca.toFormatString();
        
        // Handling the command split function
        String command = "/" + event.getCommand() + " " + args;
        //command = command.replaceAll("\\\\|", "§"); // make the pipe escapable.
        String[] commands = command.split("\\|");
        
        if ( commands.length > 1 )
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
        
        //cmd = cmd.replaceAll("§", "\\|"); //restore the escaped pipe.
        
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
