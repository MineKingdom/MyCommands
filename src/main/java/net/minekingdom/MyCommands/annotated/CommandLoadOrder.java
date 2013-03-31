package net.minekingdom.MyCommands.annotated;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandLoadOrder {

    public enum Order {
        FIRST, NORMAL, LAST;
    }

    public Order value();

}
