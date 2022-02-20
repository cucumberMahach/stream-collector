package database.entities;

import jdk.jfr.Unsigned;
import org.hibernate.type.LongType;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
    public ZonedDateTime lastCheckedTime;

    @OneToMany(mappedBy="channel")
    public Set<UserChannelEntity> usersChannels = new HashSet<>();

    @OneToMany(mappedBy="channel")
    public Set<ChannelCircleEntity> channelsCircles = new HashSet<>();
}
