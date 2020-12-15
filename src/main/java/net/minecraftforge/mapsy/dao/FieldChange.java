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
    private User user;

    @ManyToOne
    private FieldName field;

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
    public FieldName getField() { return field; }
    public Date getTimestamp() { return timestamp; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public void setUser(User user) { this.user = user; }
    public void setField(FieldName field) { this.field = field; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public void setFrom(String from) { this.from = from; }
    public void setTo(String to) { this.to = to; }
    //@formatter:on
}
