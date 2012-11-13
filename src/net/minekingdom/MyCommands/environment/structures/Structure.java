package net.minekingdom.MyCommands.environment.structures;

import java.util.*;

import net.minekingdom.MyCommands.environment.Variable;

public abstract class Structure extends Variable {

    private Map<String, Variable> variables;
    
    public Structure(String name)
    {
        super(name);
            this.variables = new HashMap<String, Variable>();
    }
    
    public Variable getVariable(String name)
    {
        String[] path = name.split("\\.");
        return getVariable(path);
    }
    
    protected Variable getVariable(String[] path)
    {
        if (path.length != 0)
        {
            Variable v = variables.get(path[0]);
            if ( v != null )
            {
                if ( path.length == 1 )
                    return v;
                
                if ( v instanceof Structure )
                {
                    String[] newPath = new String[path.length - 1];
                    System.arraycopy(path, 1, newPath, 0, newPath.length);
                    return ((Structure) v).getVariable(newPath);
                }
            }
        }
        return null;
    }
    
    public void addVariable(Variable var)
    {
        this.variables.put(var.getName(), var);
    }
    
    public void removeVariable(String name)
    {
        this.variables.remove(name);
    }
}
