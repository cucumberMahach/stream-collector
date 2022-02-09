import javax.persistence.*;

@Entity
@Cacheable
@Table(name = "testentity")
public class TestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", unique = true)
    private String s;




    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
