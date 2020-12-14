package net.minecraftforge.mapsy.dao;

import javax.persistence.*;
import java.util.List;

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

    @ManyToMany
    private List<FieldName> fields;

    @ManyToMany
    private List<MethodName> methods;

    //@formatter:off
    public long getId() { return id; }
    public MinecraftVersion getMinecraftVersion() { return minecraftVersion; }
    public String getObf() { return obf; }
    public String getMojang() { return mojang; }
    public String getSrg() { return srg; }
    public List<FieldName> getFields() { return fields; }
    public List<MethodName> getMethods() { return methods; }
    public void setMinecraftVersion(MinecraftVersion minecraftVersion) { this.minecraftVersion = minecraftVersion; }
    public void setObf(String obf) { this.obf = obf; }
    public void setMojang(String mojang) { this.mojang = mojang; }
    public void setSrg(String srg) { this.srg = srg; }
    public void setFields(List<FieldName> fields) { this.fields = fields; }
    public void setMethods(List<MethodName> methods) { this.methods = methods; }
    public void addField(FieldName field) { fields.add(field); }
    public void addMethod(MethodName method) { methods.add(method); }
    //@formatter:on
}
