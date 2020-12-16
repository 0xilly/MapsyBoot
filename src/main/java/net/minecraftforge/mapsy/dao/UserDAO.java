package net.minecraftforge.mapsy.dao;

import net.minecraftforge.mapsy.util.UserRole;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity (name = "user")
public class UserDAO {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private int id;

    @Column (nullable = false)
    private String name;

    @Column (nullable = false)
    private long discordId;

    @Column (nullable = false)
    private UserRole role = UserRole.USER;

    @ManyToMany
    private List<FieldChange> fieldChanges = new ArrayList<>();

    @ManyToMany
    private List<MethodChange> methodChanges = new ArrayList<>();

    public UserDAO() {
    }

    public UserDAO(long discordId) {
        this.discordId = discordId;
    }

    //@formatter:off
    public int getId() { return id; }
    public String getName() { return name; }
    public UserRole getRole() { return role; }
    public long getDiscordId() { return discordId; }
    public List<FieldChange> getFieldChanges() { return fieldChanges; }
    public List<MethodChange> getMethodChanges() { return methodChanges; }
    public void setName(String name) { this.name = name; }
    public void setDiscordId(long discordId) { this.discordId = discordId; }
    public void setRole(UserRole role) { this.role = role; }
    public void setFieldChanges(List<FieldChange> fieldChanges) { this.fieldChanges = fieldChanges; }
    public void setMethodChanges(List<MethodChange> methodChanges) { this.methodChanges = methodChanges; }
    public void addFieldChange(FieldChange change) { fieldChanges.add(change); }
    public void addMethodChange(MethodChange change) { methodChanges.add(change); }
    //@formatter:on
}
