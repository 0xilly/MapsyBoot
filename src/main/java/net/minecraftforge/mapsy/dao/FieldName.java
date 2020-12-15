package net.minecraftforge.mapsy.dao;

import javax.persistence.*;

/**
 * Created by covers1624 on 14/12/20.
 */
@Entity(name = "field")
public class FieldName {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private MinecraftVersion minecraftVersion;

    @ManyToOne
    private ClassName owner;

    @Column (nullable = false)
    private String obf;

    @Column (nullable = false)
    private String mojang;

    @Column (nullable = false)
    private String srg;

    private String mcp;

    //@formatter:off
    public long getId() { return id; }
    public MinecraftVersion getMinecraftVersion() { return minecraftVersion; }
    public ClassName getOwner() { return owner; }
    public String getObf() { return obf; }
    public String getMojang() { return mojang; }
    public String getSrg() { return srg; }
    public String getMcp() { return mcp; }
    public void setMinecraftVersion(MinecraftVersion minecraftVersion) { this.minecraftVersion = minecraftVersion; }
    public void setOwner(ClassName owner) { this.owner = owner; }
    public void setObf(String obf) { this.obf = obf; }
    public void setMojang(String mojang) { this.mojang = mojang; }
    public void setSrg(String srg) { this.srg = srg; }
    public void setMcp(String mcp) { this.mcp = mcp; }
    //@formatter:on

}
