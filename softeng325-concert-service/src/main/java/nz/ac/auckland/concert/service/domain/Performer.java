package nz.ac.auckland.concert.service.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Performer {

    @Id
    @GeneratedValue
    private long id;

    public long getId() {
        return id;
    }
}
