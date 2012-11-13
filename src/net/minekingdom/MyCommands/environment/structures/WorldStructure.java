package net.minekingdom.MyCommands.environment.structures;

import org.spout.api.geo.World;

import net.minekingdom.MyCommands.environment.Value;

public class WorldStructure extends Structure {

    public WorldStructure(String name, World world)
    {
        super(name);
            this.addVariable(new Value<String>("name", world.getName(), true));
            this.addVariable(new PointStructure("spawn", world.getSpawnPoint().getPosition(), this));
            this.addVariable(new Value<Integer>("height", world.getHeight(), true));
    }

    public WorldStructure(String name, WorldStructure world)
    {
        super(name);
            this.addVariable(world.getVariable("name"));
            this.addVariable(world.getVariable("spawn"));
            this.addVariable(world.getVariable("height"));
    }

    @Override
    public String getStringValue()
    {
        return this.getVariable("name").getStringValue();
    }

}
