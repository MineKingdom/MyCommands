import org.spout.api.chat.ChatSection;
import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;
import org.spout.api.command.annotated.Command;
import org.spout.api.command.annotated.CommandPermissions;
import org.spout.api.entity.Player;
import org.spout.api.exception.CommandException;
import org.spout.api.geo.World;
import org.spout.api.geo.discrete.Point;
import org.spout.api.material.BlockMaterial;

import net.minekingdom.MyCommands.MyCommands;

public class Teleport {
    
    private final MyCommands plugin;
    
    public Teleport(MyCommands plugin)
    {
        this.plugin = plugin;
    }
    
    @Command(aliases = {"teleport", "tp", "tele"}, desc = "Teleports a player to the specified destination", flags = "f", min = 1, max = 2, usage = "[Player] <Destination>[+(world:x,y,z)]")
    @CommandPermissions("mycommands.teleport")
    public void teleport(CommandContext args, CommandSource source) throws CommandException
    {
        if ( source instanceof Player )
        {
            final Player player = (Player) source;
            
            boolean force = args.hasFlag('f');
            
            String[] split;
            ChatSection targetArgs;
            String targetString;
            String modifierString = null;
            
            Player teleported;
            
            if ( args.length() == 1 )
            {
                targetArgs = args.get(0);
                teleported = player;
            }
            else
            {
                targetArgs = args.get(1);
                teleported = plugin.getServer().getPlayer(args.get(0).getPlainString(), false);
            }
            
            if ( teleported == null )
            {
                MyCommands.sendErrorMessage(source, "player_not_found");
                return;
            }
            
            split = targetArgs.getPlainString().split("\\+");
            targetString = split[0];
            if ( split.length == 2 )
            {
                modifierString = split[1];
            }
            else if ( split.length > 2 )
            {
                MyCommands.sendErrorMessage(source, "invalid_modifier");
                return;
            }
            
            Point destination;
            Point modifier;
            
            if ( targetString.startsWith("(") )
            {
                destination = this.getVector(teleported.getWorld(), targetString);
            }
            else
            {
                Player destinationPlayer = plugin.getServer().getPlayer(targetString, false);
                if ( destinationPlayer == null )
                {
                    MyCommands.sendErrorMessage(source, "player_not_found");
                    return;
                }
                
                destination = destinationPlayer.getTransform().getPosition();
            }
            
            if ( modifierString != null )
            {
                modifier = this.getVector(destination.getWorld(), modifierString);
                destination = new Point(modifier.getWorld(), 
                        destination.getX() + modifier.getX(), 
                        destination.getY() + modifier.getY(), 
                        destination.getZ() + modifier.getZ());
            }
            
            if ( destination == null )
            {
                MyCommands.sendErrorMessage(source, "invalid_destination");
                return;
            }
            
            teleported.teleport(destination);
            MyCommands.sendMessage(source, "teleported to : ", targetString);
            return;
        }
    }
    
    private Point safeLocation(Point p)
    {
        final World world = p.getWorld();
        final int x = p.getBlockX(),
                  y = p.getBlockY(),
                  z = p.getBlockZ();
        
        for ( int i = 1; i < 128; i++ )
        {
            if ( world.getBlockMaterial(x, y + i, z).equals(BlockMaterial.AIR) )
            {
                if ( i - 1 < 0 )
                    continue;
                
                if ( world.getBlockMaterial(x, y + i - 1, z).equals(BlockMaterial.AIR) )
                {
                    if ( i - 2 < 0 )
                        continue;
                    
                    if ( world.getBlockMaterial(x, y + i - 2, z).equals(BlockMaterial.SOLID) )
                    {
                        return new Point(world, x, y + i, z);
                    }
                }
            }
        }
        return p;
    }

    private Point getVector(World world, String s)
    {
        if ( s.charAt(0) != '(' )
            if ( s.charAt(s.length() - 1) != ')' )
                return null;
        
        String[] vector = s.substring(1, s.length() - 1).split(":");
        String[] modifier;
        
        String worldName = "";
        
        if ( vector.length == 1 )
        {
            modifier = vector[0].split(",");
        }
        else if ( vector.length == 2 )
        {
            worldName = vector[0];
            modifier = vector[1].split(",");
        }
        else
        {
            return null;
        }
        
        if ( modifier.length != 3 )
            return null;
        
        Float[] floatCoord = new Float[3];
        for ( int i = 0; i < 3; i++ )
        {
            try
            {
                floatCoord[i] = Float.parseFloat(modifier[i]);
            }
            catch ( NumberFormatException ex )
            {
                return null;
            }
        }
        
        if ( !worldName.equals("") )
        {
            World newWorld = MyCommands.getInstance().getServer().getWorld(worldName);
            if ( newWorld == null )
                return null;
            
            world = newWorld;
        }
        
        return new Point(world, floatCoord[0], floatCoord[1], floatCoord[2]);
    }
    
}
