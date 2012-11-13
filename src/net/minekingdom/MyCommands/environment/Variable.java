package net.minekingdom.MyCommands.environment;

public abstract class Variable {
    
    private String name;
    
    public Variable(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    public abstract String getStringValue();
}
