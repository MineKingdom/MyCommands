import net.minekingdom.MyCommands.MyCommands;

import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;
import org.spout.api.command.annotated.Command;
import org.spout.api.command.annotated.CommandPermissions;
import org.spout.api.entity.Player;
import org.spout.api.exception.CommandException;
import org.spout.api.geo.World;
import org.spout.api.geo.cuboid.Block;

public class Ascend {
    
    @SuppressWarnings("unused")
    private final MyCommands plugin;
    
    public Ascend(MyCommands plugin)
    {
        this.plugin = plugin;
    }
    
    @Command(aliases = {"ascend", "asc"}, desc = "Ascends a level")
    @CommandPermissions("mycommands.ascend")
    public void ascend(CommandContext args, CommandSource source) throws CommandException
    {
        if ( source instanceof Player )
        {
            final Player player = (Player) source;
            final World world = player.getWorld();
            final int x = player.getTransform().getPosition().getBlockX(),
                      y = player.getTransform().getPosition().getBlockY(),
                      z = player.getTransform().getPosition().getBlockZ();
            
            for ( int i = y; i < y + 256; i++ )
            {
                Block b1 = world.getBlock(x, i, z),
                      b2 = world.getBlock(x, i+1, z),
                      b3 = world.getBlock(x, i+2, z);
                if ( b1.getMaterial().isSolid() && !b2.getMaterial().isSolid() && !b3.getMaterial().isSolid() )
                {
                    player.teleport(b2.getPosition());
                    break;
                }
            }
            
            MyCommands.sendMessage((Player) source, ChatStyle.CYAN, "Ascended a level.");
        }
    }

}
