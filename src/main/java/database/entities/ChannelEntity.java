package database.entities;

import jdk.jfr.Unsigned;
import org.hibernate.type.LongType;

import javax.persistence.*;
import java.util.Date;

@Entity
@Cacheable
@Table(name = "channels")
public class ChannelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Unsigned
    public Long id;

    @ManyToOne
    @JoinColumn(name="lastCircle_id")
    public CircleEntity lastCircle;

    @Column(name = "name", unique = true, nullable = false)
    public String name;

    @Column(name = "lastCheckedTime")
    public Date lastCheckedTime;
}
