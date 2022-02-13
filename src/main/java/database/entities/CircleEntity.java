package database.entities;

import jdk.jfr.Unsigned;
import org.hibernate.type.LongType;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Cacheable
@Table(name = "circles")
public class CircleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Unsigned
    public LongType id;

    @Column(name = "startTime")
    public Date startTime;

    @Column(name = "endTime")
    public Date endTime;

    @Column(name = "totalChannels")
    public int totalChannels;

    @OneToMany(mappedBy="lastCircle")
    public Set<ChannelEntity> channels = new HashSet<>();

    @OneToMany(mappedBy="firstCircle")
    public Set<UserChannelEntity> usersChannelsFirst = new HashSet<>();

    @OneToMany(mappedBy="lastCircle")
    public Set<UserChannelEntity> usersChannelsLast = new HashSet<>();
}