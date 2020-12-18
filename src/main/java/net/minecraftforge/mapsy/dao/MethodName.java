package net.minecraftforge.mapsy.dao;

import net.minecraftforge.mapsy.util.MappingSide;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by covers1624 on 14/12/20.
 */
@Entity (name = "method")
public class MethodName {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private MinecraftVersion minecraftVersion;

    @ManyToMany
    private List<ClassName> owners = new ArrayList<>();

    @Column (nullable = false)
    private MappingSide side = MappingSide.BOTH;

    @Column (nullable = false)
    private String obf;

    @Column (nullable = false, columnDefinition = "TEXT")
    private String obfDesciptor;

    @Column (nullable = false)
    private String mojang;

    @Column (nullable = false)
    private String srg;

    @Column (nullable = false, columnDefinition = "TEXT")
    private String descriptor;

    private String mcp;

    private boolean locked;

    @Column (columnDefinition = "TEXT")
    private String description;

    public MethodName() {
    }

    //@formatter:off
    public long getId() { return id; }
    public MinecraftVersion getMinecraftVersion() { return minecraftVersion; }
    public List<ClassName> getOwners() { return owners; }
    public MappingSide getSide() { return side; }
    public String getObf() { return obf; }
    public String getObfDesciptor() { return obfDesciptor; }
    public String getMojang() { return mojang; }
    public String getSrg() { return srg; }
    public String getDescriptor() { return descriptor; }
    public String getMcp() { return mcp; }
    public boolean isLocked() { return locked; }
    public String getDescription() { return description; }
    public void setMinecraftVersion(MinecraftVersion minecraftVersion) { this.minecraftVersion = minecraftVersion; }
    public void setOwners(List<ClassName> owners) { this.owners = owners; }
    public void setSide(MappingSide side) { this.side = side; }
    public void setObf(String obf) { this.obf = obf; }
    public void setObfDesciptor(String obfDesc) { this.obfDesciptor = obfDesc; }
    public void setMojang(String mojang) { this.mojang = mojang; }
    public void setSrg(String srg) { this.srg = srg; }
    public void setDescriptor(String desc) { this.descriptor = desc; }
    public void setMcp(String mcp) { this.mcp = mcp; }
    public void addOwner(ClassName owner) { owners.add(owner); }
    public void setLocked(boolean locked) { this.locked = locked; }
    public void setDescription(String description) { this.description = description; }
    //@formatter:on
}
