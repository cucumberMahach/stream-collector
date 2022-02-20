package database.entities;

import jdk.jfr.Unsigned;
import org.hibernate.type.LongType;

import javax.persistence.*;
import java.time.ZonedDateTime;
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
    public Long id;

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
    public CircleEntity lastCircle;

    @ManyToOne
    @JoinColumn(name="type_id", nullable = false)
    public UserTypeEntity type;
}
