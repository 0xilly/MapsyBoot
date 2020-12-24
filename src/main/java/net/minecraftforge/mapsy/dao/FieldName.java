package net.minecraftforge.mapsy.dao;

import net.minecraftforge.mapsy.util.MappingSide;

import javax.persistence.*;

/**
 * Created by covers1624 on 14/12/20.
 */
@Entity (name = "field")
public class FieldName {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private MinecraftVersion minecraftVersion;

    @ManyToOne
    private ClassName owner;

    @Column (nullable = false)
    private MappingSide side = MappingSide.BOTH;

    @Column (nullable = false)
    private String obf;

    @Column (nullable = false)
    private String mojang;

    @Column (nullable = false)
    private String srg;

    private String mcp;

    private boolean locked;

    @Column (columnDefinition = "TEXT")
    private String description;

    //@formatter:off
    public long getId() { return id; }
    public MinecraftVersion getMinecraftVersion() { return minecraftVersion; }
    public ClassName getOwner() { return owner; }
    public MappingSide getSide() { return side; }
    public String getObf() { return obf; }
    public String getMojang() { return mojang; }
    public String getSrg() { return srg; }
    public String getMcp() { return mcp; }
    public boolean isLocked() { return locked; }
    public String getDescription() { return description; }
    public void setMinecraftVersion(MinecraftVersion minecraftVersion) { this.minecraftVersion = minecraftVersion; }
    public void setOwner(ClassName owner) { this.owner = owner; }
    public void setSide(MappingSide side) { this.side = side; }
    public void setObf(String obf) { this.obf = obf; }
    public void setMojang(String mojang) { this.mojang = mojang; }
    public void setSrg(String srg) { this.srg = srg; }
    public void setMcp(String mcp) { this.mcp = mcp; }
    public void setLocked(boolean locked) { this.locked = locked; }
    public void setDescription(String description) { this.description = description; }
    //@formatter:on

    public FieldName fork() {
        FieldName fName = new FieldName();
        fName.setObf(getObf());
        fName.setMojang(getMojang());
        fName.setSrg(getSrg());
        fName.setMcp(getMcp());
        fName.setLocked(isLocked());
        fName.setDescription(getDescription());
        return fName;
    }
}
