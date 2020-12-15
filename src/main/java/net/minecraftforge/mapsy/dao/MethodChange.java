package net.minecraftforge.mapsy.dao;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by covers1624 on 14/12/20.
 */
@Entity (name = "history_change")
public class MethodChange {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @Column (nullable = false)
    private User user;

    @ManyToOne
    @Column (nullable = false)
    private MethodName method;

    @Column (nullable = false)
    @Temporal (TemporalType.TIMESTAMP)
    private Date timestamp = new Date();

    @Column (nullable = false)
    private String from;

    @Column (nullable = false)
    private String to;

    //@formatter:off
    public long getId() { return id; }
    public User getUser() { return user; }
    public MethodName getMethod() { return method; }
    public Date getTimestamp() { return timestamp; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public void setUser(User user) { this.user = user; }
    public void setMethod(MethodName method) { this.method = method; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public void setFrom(String from) { this.from = from; }
    public void setTo(String to) { this.to = to; }
    //@formatter:on
}
