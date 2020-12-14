package net.minecraftforge.mapsy.dao;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by covers1624 on 14/12/20.
 */
@Entity (name = "field")
public class MethodName {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private MinecraftVersion minecraftVersion;

    @ManyToMany
    private List<ClassName> owners = new ArrayList<>();

    @Column (nullable = false)
    private String obf;

    @Column (nullable = false)
    private String obfDesc;

    @Column (nullable = false)
    private String mojang;

    @Column (nullable = false)
    private String srg;

    @Column (nullable = false)
    private String desc;

    private String mcp;

    public MethodName() {
    }

    //@formatter:off
    public long getId() { return id; }
    public MinecraftVersion getMinecraftVersion() { return minecraftVersion; }
    public List<ClassName> getOwners() { return owners; }
    public String getObf() { return obf; }
    public String getObfDesc() { return obfDesc; }
    public String getMojang() { return mojang; }
    public String getSrg() { return srg; }
    public String getDesc() { return desc; }
    public String getMcp() { return mcp; }
    public void setMinecraftVersion(MinecraftVersion minecraftVersion) { this.minecraftVersion = minecraftVersion; }
    public void setOwners(List<ClassName> owners) { this.owners = owners; }
    public void setObf(String obf) { this.obf = obf; }
    public void setObfDesc(String obfDesc) { this.obfDesc = obfDesc; }
    public void setMojang(String mojang) { this.mojang = mojang; }
    public void setSrg(String srg) { this.srg = srg; }
    public void setDesc(String desc) { this.desc = desc; }
    public void setMcp(String mcp) { this.mcp = mcp; }
    public void addOwner(ClassName owner) { owners.add(owner); }
    //@formatter:on
}
