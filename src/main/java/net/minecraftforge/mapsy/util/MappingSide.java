package net.minecraftforge.mapsy.util;

/**
 * Created by covers1624 on 15/12/20.
 */
public enum MappingSide {
    CLIENT,
    SERVER,
    BOTH;

    public MappingSide opposite() {
        return switch (this) {
            case CLIENT -> SERVER;
            case SERVER -> CLIENT;
            case BOTH -> null;
        };
    }

}
