package me.jetby.libb.command.annotations.messages;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface InsufficientArgs {
    String value() default "Insufficient arguments";
}
