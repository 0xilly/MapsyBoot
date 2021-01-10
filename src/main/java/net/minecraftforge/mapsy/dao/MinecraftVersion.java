package net.minecraftforge.mapsy.dao;

import javax.persistence.*;

/**
 * Created by covers1624 on 14/12/20.
 */
@Entity (name = "minecraft_version")
@Table (uniqueConstraints = @UniqueConstraint (columnNames = { "name", "revision" }))
public class MinecraftVersion {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private int revision;

    private boolean latest;

    public MinecraftVersion() {}

    public MinecraftVersion(String name) {
        this.name = name;
    }

    //@formatter:off
    public long getId() { return id; }
    public String getName() { return name; }
    public int getRevision() { return revision; }
    public boolean isLatest() { return latest; }
    public void setName(String name) { this.name = name; }
    public void setRevision(int revision) { this.revision = revision; }
    public void setLatest(boolean latest) { this.latest = latest; }
    //@formatter:on
}
