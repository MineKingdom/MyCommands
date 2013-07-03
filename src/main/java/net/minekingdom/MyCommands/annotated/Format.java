package net.minekingdom.MyCommands.annotated;

import net.minekingdom.MyCommands.formatting.Formatter;

public @interface Format {
    public Class<? extends Formatter>[] value();
}
