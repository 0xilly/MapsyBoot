package net.minecraftforge.mapsy.service.discord.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CMDMeta {

    String name();

    String description();

    String usage();

    Permission perm();

    public static enum Permission {
        USER,
        MOD,
        ADMIN
    }
}
