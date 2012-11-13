package net.minekingdom.MyCommands.environment.structures;

import org.spout.api.entity.Player;

import net.minekingdom.MyCommands.environment.Value;
import net.minekingdom.MyCommands.environment.Variable;

public class PlayerStructure extends Structure {
    
    private final Player player;

    public PlayerStructure(String name, Player player)
    {
        super(name);
            this.player = player;
            this.addVariable(new Value<String>("name", player.getName(), true));
            this.addVariable(new Value<String>("ip", player.getAddress().getHostAddress(), true));
    }
    
    @Override
    protected Variable getVariable(String[] path)
    {
        if ( path[0].equals("pos") )
        {
            if ( path.length == 1 )
            {
                return new PointStructure("pos", player.getTransform().getPosition());
            }
            else
            {
                String[] newPath = new String[path.length - 1];
                System.arraycopy(path, 1, newPath, 0, newPath.length);
                return (new PointStructure("pos", player.getTransform().getPosition())).getVariable(newPath);
            }
        }
        return super.getVariable(path);
    }

    @Override
    public String getStringValue()
    {
        return this.getVariable("name").getStringValue();
    }

}
