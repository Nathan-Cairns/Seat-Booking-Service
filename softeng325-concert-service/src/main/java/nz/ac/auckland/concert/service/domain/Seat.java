package nz.ac.auckland.concert.service.domain;

import javax.persistence.*;

@Entity
@Table(name = "SEATS")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="SID", nullable = false)
    private long id;

    public Seat() {}

    public void setId(long id) {
        this.id = id;
    }
}
