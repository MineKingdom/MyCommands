package net.minekingdom.MyCommands.annotated;

public @interface NestedCommand {
    public Class<?> value();

    public boolean ignoreBody() default true;
}
