package net.minekingdom.MyCommands;

import java.util.LinkedList;

import org.spout.api.command.CommandSource;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.Order;
import org.spout.api.event.server.PreCommandEvent;

public class CoreListener implements Listener {

    @EventHandler(order = Order.EARLIEST)
    public void onCommand(PreCommandEvent event) {
        final CommandSource source = event.getCommandSource();

        // Handling the command split function
        LinkedList<String> commands = new LinkedList<String>();
        {
            String command = "/" + event.getCommand() + " " + event.getArguments().getJoinedString(0);
            int split = -1, last = 0;
            while ((split = command.indexOf('|', split)) != -1) {
                if (split > 0 && command.charAt(split - 1) == '\\') {
                    continue;
                }
                commands.add(command.substring(last, split));
                last = split + 1;
            }
            commands.add(command.substring(last));
        }

        if (commands.size() > 1) {
            for (String cmd : commands) {
                if (!cmd.startsWith("/")) {
                    cmd = "say " + cmd;
                } else {
                    cmd = cmd.substring(1);
                }
                
                int space = cmd.indexOf(' ');
                if (space == -1) {
                    source.processCommand(cmd, "");
                } else {
                    source.processCommand(cmd.substring(0, space), cmd.substring(space + 1).split(" "));
                }
            }

            event.setCancelled(true);
            return;
        }
    }

}
