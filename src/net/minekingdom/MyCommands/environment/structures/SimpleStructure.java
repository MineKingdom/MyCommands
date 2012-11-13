package net.minekingdom.MyCommands.environment.structures;


public class SimpleStructure extends Structure {

    public SimpleStructure(String name)
    {
        super(name);
    }

    @Override
    public String getStringValue()
    {
        return getName();
    }

}
