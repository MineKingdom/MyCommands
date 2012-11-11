import net.minekingdom.MyCommands.MyCommands;

import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;
import org.spout.api.command.annotated.Command;
import org.spout.api.command.annotated.CommandPermissions;
import org.spout.api.entity.Player;
import org.spout.api.exception.CommandException;
import org.spout.api.geo.discrete.Point;

public class Whois {
    
    private final MyCommands plugin;
    
    public Whois(MyCommands plugin)
    {
        this.plugin = plugin;
    }
    
    @Command(aliases = {"whois", "info"}, desc = "Gets information on a player", min = 1, max = 1)
    @CommandPermissions("mycommands.whois")
    public void whois(CommandContext args, CommandSource source) throws CommandException
    {
        if ( source instanceof Player )
        {
            final Player player = (Player) source;
            final Player target = plugin.getServer().getPlayer(args.get(0).getPlainString(), false);
            
            if ( target == null )
            {
                MyCommands.sendErrorMessage(source, "Player " + args.get(0).getPlainString() + " could not be found.");
                return;
            }
            
            Point p = target.getTransform().getPosition();
            
            MyCommands.sendMessage(player, ChatStyle.CYAN, "Player : " + target.getName() );
            MyCommands.sendMessage(player, ChatStyle.CYAN, "IP Address : " + target.getAddress().getHostAddress() );
            MyCommands.sendMessage(player, ChatStyle.CYAN, "world : " + target.getWorld().getName() + ", position : " + p.getX() + ", " + p.getY() + ", " + p.getZ());
        }
    }

}
