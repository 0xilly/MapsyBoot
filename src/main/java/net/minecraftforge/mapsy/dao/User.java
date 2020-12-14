package net.minecraftforge.mapsy.dao;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity (name = "user")
public class User {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private long discordId;

    @Column(nullable = false)
    private String role;

    @ManyToMany
    @Column(nullable = false)
    private List<FieldChange> fieldChanges = new ArrayList<>();

    @ManyToMany
    @Column(nullable = false)
    private List<MethodChange> methodChanges = new ArrayList<>();

    //@formatter:off
    public int getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public long getDiscordId() { return discordId; }
    public List<FieldChange> getFieldChanges() { return fieldChanges; }
    public List<MethodChange> getMethodChanges() { return methodChanges; }
    public void setName(String name) { this.name = name; }
    public void setDiscordId(long discordId) { this.discordId = discordId; }
    public void setRole(String role) { this.role = role; }
    public void setFieldChanges(List<FieldChange> fieldChanges) { this.fieldChanges = fieldChanges; }
    public void setMethodChanges(List<MethodChange> methodChanges) { this.methodChanges = methodChanges; }
    public void addFieldChange(FieldChange change) { fieldChanges.add(change); }
    public void addMethodChange(MethodChange change) { methodChanges.add(change); }
    //@formatter:on
}
