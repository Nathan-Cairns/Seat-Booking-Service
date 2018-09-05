package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.PriceBand;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name="RESERVATIONS")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RID", nullable = false)
    private long id;

    @ManyToOne
    @JoinColumn(name = "USER", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "CONCERT", nullable = false)
    private Concert concert;

    @Column(name = "PRICE_BAND", nullable = false)
    @Enumerated(EnumType.STRING)
    private PriceBand priceBand;

    @ElementCollection
    @CollectionTable(name = "RESERVERED_SEATS", joinColumns = @JoinColumn(name = "rid"))
    @Column(name = "SEATS", nullable = false)
    private Set<Seat> seats;

    private Boolean confirmed = false;

    public Reservation() {

    }

    public Reservation(User user, Concert concert, PriceBand priceBand, Set<Seat> seats) {
        this.user = user;
        this.concert = concert;
        this.priceBand = priceBand;
        this.seats = seats;
    }

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Concert getConcert() {
        return concert;
    }

    public void setConcert(Concert concert) {
        this.concert = concert;
    }

    public PriceBand getPriceBand() {
        return priceBand;
    }

    public void setPriceBand(PriceBand priceBand) {
        this.priceBand = priceBand;
    }

    public Set<Seat> getSeats() {
        return seats;
    }

    public void setSeats(Set<Seat> seats) {
        this.seats = seats;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }
}
