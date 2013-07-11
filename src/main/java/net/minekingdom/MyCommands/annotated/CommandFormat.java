package net.minekingdom.MyCommands.annotated;

import net.minekingdom.MyCommands.formatting.Formatter;

public @interface CommandFormat {
    public Class<? extends Formatter>[] value();
}
