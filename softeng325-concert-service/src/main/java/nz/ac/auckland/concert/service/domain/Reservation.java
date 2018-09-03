package nz.ac.auckland.concert.service.domain;

import javax.persistence.*;

@Entity
@Table(name="RESERVATIONS")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID", nullable = false)
    private long id;

    public Reservation() {}

    public long getId() {
        return id;
    }
}
