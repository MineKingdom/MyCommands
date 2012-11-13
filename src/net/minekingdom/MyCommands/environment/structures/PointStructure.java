package net.minekingdom.MyCommands.environment.structures;

import org.spout.api.geo.discrete.Point;

import net.minekingdom.MyCommands.Environment;
import net.minekingdom.MyCommands.environment.Value;

public class PointStructure extends Structure {
    
    public PointStructure(String name, Point p)
    {
        super(name);
            this.addVariable(new Value<Float>("x", p.getX(), false));
            this.addVariable(new Value<Float>("y", p.getY(), false));
            this.addVariable(new Value<Float>("z", p.getZ(), false));
            this.addVariable(new WorldStructure("world", (WorldStructure) Environment.getVariable("worlds." + p.getWorld().getName())));
    }
    
    public PointStructure(String name, Point p, WorldStructure world)
    {
        super(name);
            this.addVariable(new Value<Float>("x", p.getX(), false));
            this.addVariable(new Value<Float>("y", p.getY(), false));
            this.addVariable(new Value<Float>("z", p.getZ(), false));
            this.addVariable(world);
    }

    @Override
    public String getStringValue()
    {
        return "(" + this.getVariable("world").getStringValue() + ":" + this.getVariable("x").getStringValue() + "," + this.getVariable("y").getStringValue() + "," + this.getVariable("z").getStringValue() + ")";
    }

}
