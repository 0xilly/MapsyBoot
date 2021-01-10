package net.minecraftforge.mapsy.dao;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by covers1624 on 14/12/20.
 */
@Entity (name = "method_change")
public class MethodChange {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private UserDAO user;

    @ManyToOne
    private MethodName method;

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
    public MethodName getMethod() { return method; }
    public Date getTimestamp() { return timestamp; }
    public String getOldName() { return oldName; }
    public String getNewName() { return newName; }
    public void setUser(UserDAO user) { this.user = user; }
    public void setMethod(MethodName method) { this.method = method; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public void setOldName(String from) { oldName = from; }
    public void setNewName(String to) { newName = to; }
    //@formatter:on

    public MethodChange fork(MethodName newMethod) {
        MethodChange mChange = new MethodChange();
        mChange.setUser(getUser());
        mChange.setMethod(newMethod);
        mChange.setTimestamp(getTimestamp());
        mChange.setOldName(getOldName());
        mChange.setNewName(getNewName());
        return mChange;
    }
}
