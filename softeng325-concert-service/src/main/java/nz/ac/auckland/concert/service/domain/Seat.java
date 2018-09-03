package nz.ac.auckland.concert.service.domain;

import javax.persistence.Embeddable;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Embeddable
public class Seat {

    @Id
    @GeneratedValue
    private long id;

    public long getId() {
        return id;
    }
}
