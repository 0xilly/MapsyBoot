package net.minecraftforge.mapsy.dao;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by covers1624 on 14/12/20.
 */
@Entity (name = "field_change")
public class FieldChange {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private UserDAO user;

    @ManyToOne
    private FieldName field;

    @Column (nullable = false)
    @Temporal (TemporalType.TIMESTAMP)
    private Date timestamp = new Date();

    @Column (nullable = false)
    private String oldName;

    @Column (nullable = false)
    private String newName;

    //@formatter:off
    public long getId() { return id; }
    public UserDAO getUser() { return user; }
    public FieldName getField() { return field; }
    public Date getTimestamp() { return timestamp; }
    public String getOldName() { return oldName; }
    public String getNewName() { return newName; }
    public void setUser(UserDAO user) { this.user = user; }
    public void setField(FieldName field) { this.field = field; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public void setOldName(String from) { this.oldName = from; }
    public void setNewName(String to) { this.newName = to; }
    //@formatter:on
}
