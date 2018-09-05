package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.service.domain.jpa.LocalDateTimeConverter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name="RESERVATIONS")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RID", nullable = false)
    private long id;

    @ManyToOne
    @JoinColumn(name = "USER")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne
    @JoinColumn(name = "CONCERT", nullable = false)
    private Concert concert;

    @Column(name = "PRICE_BAND", nullable = false)
    @Enumerated(EnumType.STRING)
    private PriceBand priceBand;

    @Column(name = "DATE_TIME")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime dateTime;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "_reservation")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Seat> seats;

    @Column(name = "CONFIRMED", nullable = false)
    private Boolean confirmed = false;

    public Reservation() {

    }

    public Reservation(User user, Concert concert, PriceBand priceBand, Set<Seat> seats, LocalDateTime dateTime) {
        this.user = user;
        this.concert = concert;
        this.priceBand = priceBand;
        this.seats = seats;
        this.dateTime = dateTime;
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

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
