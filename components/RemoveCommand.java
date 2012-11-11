import java.util.List;

import net.minekingdom.MyCommands.MyCommands;

import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;
import org.spout.api.command.annotated.Command;
import org.spout.api.command.annotated.CommandPermissions;
import org.spout.api.entity.Entity;
import org.spout.api.entity.Player;
import org.spout.api.exception.CommandException;
import org.spout.api.geo.World;
import org.spout.api.geo.discrete.Point;
import org.spout.api.material.BlockMaterial;

public class RemoveCommand {
    
    private final MyCommands plugin;
    
    public RemoveCommand(MyCommands plugin)
    {
        this.plugin = plugin;
    }
    
    @Command(aliases = {"remove", "rm"}, desc = "Removes entities around the source.")
    @CommandPermissions("mycommands.remove")
    public void remove(CommandContext args, CommandSource source) throws CommandException
    {
        if ( source instanceof Player )
        {
            final Player player = (Player) source;
            final World world = player.getWorld();
            final int x = player.getTransform().getPosition().getBlockX(),
                      y = player.getTransform().getPosition().getBlockY(),
                      z = player.getTransform().getPosition().getBlockZ();
            
            List<Entity> entities = player.getChunk().getEntities();
            
            MyCommands.sendMessage((Player) source, ChatStyle.CYAN, "Removed " + " entities.");
        }
    }

}
