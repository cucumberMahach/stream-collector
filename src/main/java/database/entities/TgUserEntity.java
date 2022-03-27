package database.entities;

import jdk.jfr.Unsigned;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Cacheable
@Table(name = "tgusers")
public class TgUserEntity implements Cloneable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Unsigned
    public Long id;

    @Column(name = "tg_id", unique = true, nullable = false)
    public String tgId;

    @Column(name = "firstName", nullable = false)
    public String firstName;

    @Column(name = "lastName")
    public String lastName;

    @Column(name = "username", nullable = false)
    public String username;

    @Column(name = "language", nullable = false)
    public String language;

    @Column(name = "messagesTotal", nullable = false)
    @Unsigned
    public Long messagesTotal = 0L;

    @Column(name = "banned", nullable = false)
    public boolean banned = false;

    @Column(name = "state", nullable = false)
    public String state;

    @Column(name = "lastOnlineTime", nullable = false)
    public ZonedDateTime lastOnlineTime;

    @OneToMany(mappedBy="tgUser")
    public Set<TgHistoryEntity> tgHistory = new HashSet<>();

    public TgUserEntity copy(){
        try {
            return (TgUserEntity) clone();
        }catch (CloneNotSupportedException ex){
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TgUserEntity tgUser = (TgUserEntity) o;
        return banned == tgUser.banned && Objects.equals(id, tgUser.id) && Objects.equals(tgId, tgUser.tgId) && Objects.equals(firstName, tgUser.firstName) && Objects.equals(lastName, tgUser.lastName) && Objects.equals(username, tgUser.username) && Objects.equals(language, tgUser.language) && Objects.equals(messagesTotal, tgUser.messagesTotal) && Objects.equals(state, tgUser.state) && Objects.equals(lastOnlineTime, tgUser.lastOnlineTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tgId, firstName, lastName, username, language, messagesTotal, banned, state);
    }
}
