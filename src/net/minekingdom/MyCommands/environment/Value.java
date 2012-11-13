package net.minekingdom.MyCommands.environment;

public class Value<T> extends Variable {

    private Class<T> type;
    private T value;
    private boolean immutable;
    
    @SuppressWarnings("unchecked")
    public Value(String name, T value, boolean immutable)
    {
        super(name);
            this.value = value;
            this.type = (Class<T>) value.getClass();
    }
    
    public T getValue()
    {
        return this.value;
    }
    
    public void setValue(T value)
    {
        if ( immutable )
            throw new UnsupportedOperationException("This object is immutable, you cannot modifiy it.");
        this.value = value;
    }
    
    public String getStringValue()
    {
        return this.value.toString();
    }

    public Class<T> getType()
    {
        return type;
    }
}
