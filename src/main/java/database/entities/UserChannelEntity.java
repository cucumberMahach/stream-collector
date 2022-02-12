package database.entities;

import jdk.jfr.Unsigned;
import org.hibernate.type.LongType;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Cacheable
@Table(name = "users_channels")
public class UserChannelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Unsigned
    public LongType id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    public UserEntity user;

    @ManyToOne
    @JoinColumn(name="channel_id", nullable = false)
    public ChannelEntity channel;

    @ManyToOne
    @JoinColumn(name="firstCircle_id")
    public CircleEntity firstCircle;

    @ManyToOne
    @JoinColumn(name="lastCircle_id")
    public ChannelEntity lastCircle;

    @Column(name = "type", nullable = false)
    public String type;

    @Column(name = "firstOnlineTime", nullable = false)
    public Date firstOnlineTime;

    @Column(name = "lastOnlineTime", nullable = false)
    public Date lastOnlineTime;
}
