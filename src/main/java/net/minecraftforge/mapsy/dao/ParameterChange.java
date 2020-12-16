package net.minecraftforge.mapsy.dao;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by covers1624 on 14/12/20.
 */
@Entity (name = "parameter_change")
public class ParameterChange {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private ParameterName parameter;

    @Column (nullable = false)
    @Temporal (TemporalType.TIMESTAMP)
    private Date timestamp = new Date();

    @Column (nullable = false)
    private String oldName;

    @Column (nullable = false)
    private String newName;

    //@formatter:off
    public long getId() { return id; }
    public User getUser() { return user; }
    public ParameterName getParameter() { return parameter; }
    public Date getTimestamp() { return timestamp; }
    public String getOldName() { return oldName; }
    public String getNewName() { return newName; }
    public void setUser(User user) { this.user = user; }
    public void setParameter(ParameterName parameter) { this.parameter = parameter; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public void setOldName(String from) { this.oldName = from; }
    public void setNewName(String to) { this.newName = to; }
    //@formatter:on
}
