package net.minekingdom.MyCommands.annotated;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.spout.api.util.config.Configuration;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandConfiguration {
    public Class<? extends Configuration> value();
}
