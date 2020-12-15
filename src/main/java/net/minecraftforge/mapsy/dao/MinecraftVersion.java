package net.minecraftforge.mapsy.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by covers1624 on 14/12/20.
 */
@Entity (name = "minecraft_version")
public class MinecraftVersion {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    //@formatter:off
    public MinecraftVersion() {}
    public MinecraftVersion(String name) { this.name = name; }
    public long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    //@formatter:on
}
