package net.minecraftforge.mapsy.dao;

import javax.persistence.*;
import java.util.List;

/**
 * Created by covers1624 on 14/12/20.
 */
@Entity(name = "minecraft_version")
public class MinecraftVersion {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @OneToMany
    private List<ClassName> classes;

    @OneToMany
    private List<FieldName> fields;

    @OneToMany
    private List<MethodName> methods;

    //@formatter:off
    public long getId() { return id; }
    public String getName() { return name; }
    public List<ClassName> getClasses() { return classes; }
    public List<FieldName> getFields() { return fields; }
    public List<MethodName> getMethods() { return methods; }
    public void setName(String name) { this.name = name; }
    public void setClasses(List<ClassName> classes) { this.classes = classes; }
    public void setFields(List<FieldName> fields) { this.fields = fields; }
    public void setMethods(List<MethodName> methods) { this.methods = methods; }
    public void addClass(ClassName clazz) { classes.add(clazz); }
    public void addField(FieldName field) { fields.add(field); }
    public void addMethod(MethodName method) { methods.add(method); }
    //@formatter:on
}
