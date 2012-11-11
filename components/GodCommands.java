

import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;
import org.spout.api.command.annotated.Command;
import org.spout.api.command.annotated.CommandPermissions;
import org.spout.api.entity.Player;
import org.spout.api.exception.CommandException;

public class GodCommands {

    @Command(aliases = {"godmode", "god"}, desc = "Grants immortality to a player")
    @CommandPermissions("mycommands.god")
    public static void godmode(CommandContext args, CommandSource source) throws CommandException
    {
        if ( source instanceof Player )
        {
            if ( args.length() == 0 )
            {
                ((Player) source).getData().put("god", true);
            }
        }
    }
    
    
}
