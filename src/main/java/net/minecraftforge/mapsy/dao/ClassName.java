package net.minecraftforge.mapsy.dao;

import javax.persistence.*;

/**
 * Created by covers1624 on 14/12/20.
 */
@Entity (name = "minecraft_version")
public class ClassName {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private MinecraftVersion minecraftVersion;

    @Column (nullable = false)
    private String obf;

    @Column (nullable = false)
    private String mojang;

    @Column (nullable = false)
    private String srg;

    //@formatter:off
    public long getId() { return id; }
    public MinecraftVersion getMinecraftVersion() { return minecraftVersion; }
    public String getObf() { return obf; }
    public String getMojang() { return mojang; }
    public String getSrg() { return srg; }
    public void setMinecraftVersion(MinecraftVersion minecraftVersion) { this.minecraftVersion = minecraftVersion; }
    public void setObf(String obf) { this.obf = obf; }
    public void setMojang(String mojang) { this.mojang = mojang; }
    public void setSrg(String srg) { this.srg = srg; }
    //@formatter:on
}
