package net.minecraftforge.mapsy.dao;

import javax.persistence.*;

/**
 * Created by covers1624 on 16/12/20.
 */
@Entity (name = "parameter")
public class ParameterName {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private MinecraftVersion minecraftVersion;

    @ManyToOne
    private MethodName owner;

    @Column (nullable = false)
    private String srg;

    private String mcp;

    private boolean locked;

    //@formatter:off
    public long getId() { return id; }
    public MinecraftVersion getMinecraftVersion() { return minecraftVersion; }
    public MethodName getOwner() { return owner; }
    public String getSrg() { return srg; }
    public String getMcp() { return mcp; }
    public boolean isLocked() { return locked; }
    public void setMinecraftVersion(MinecraftVersion minecraftVersion) { this.minecraftVersion = minecraftVersion; }
    public void setOwner(MethodName owner) { this.owner = owner; }
    public void setSrg(String srg) { this.srg = srg; }
    public void setMcp(String mcp) { this.mcp = mcp; }
    public void setLocked(boolean locked) { this.locked = locked; }
    //@formatter:on

    public ParameterName fork() {
        ParameterName pName = new ParameterName();
        pName.setSrg(getSrg());
        pName.setMcp(getMcp());
        pName.setLocked(isLocked());
        return pName;
    }

}
