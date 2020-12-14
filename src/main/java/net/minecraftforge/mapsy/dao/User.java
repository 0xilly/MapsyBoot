package net.minecraftforge.mapsy.dao;


import javax.persistence.*;

@Entity
public class User {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private String name;

    @Column
    private String discordId;

    @Column
    private String role;

    public User() {}

    public User(int id, String name, String discordId, String role) {
        this.id = id;
        this.name = name;
        this.discordId = discordId;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
