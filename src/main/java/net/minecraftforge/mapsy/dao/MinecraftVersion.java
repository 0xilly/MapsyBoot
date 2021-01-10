package net.minecraftforge.mapsy.dao;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;

/**
 * Created by covers1624 on 14/12/20.
 */
@Entity (name = "minecraft_version")
@Table (uniqueConstraints = @UniqueConstraint (columnNames = { "name", "revision" }))
public class MinecraftVersion implements Comparable<MinecraftVersion> {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private int revision;

    private boolean latest;

    private boolean locked;

    public MinecraftVersion() {}

    public MinecraftVersion(String name) {
        this.name = name;
    }

    //@formatter:off
    public long getId() { return id; }
    public String getName() { return name; }
    public int getRevision() { return revision; }
    public boolean isLatest() { return latest; }
    public boolean isLocked() { return locked; }
    public void setName(String name) { this.name = name; }
    public void setRevision(int revision) { this.revision = revision; }
    public void setLatest(boolean latest) { this.latest = latest; }
    public void setLocked(boolean locked) { this.locked = locked; }
    //@formatter:on

    @Override
    public int compareTo(@NotNull MinecraftVersion o) {
        return new DefaultArtifactVersion(getName()).compareTo(new DefaultArtifactVersion(o.getName()));
    }
}
