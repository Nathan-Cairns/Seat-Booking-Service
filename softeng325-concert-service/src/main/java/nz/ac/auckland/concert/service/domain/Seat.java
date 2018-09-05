package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.common.types.SeatStatus;
import nz.ac.auckland.concert.service.domain.jpa.SeatNumberConverter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "SEATS")
public class Seat {

    @Id
    @ManyToOne
    @Column(name = "CONCERT")
    private Concert concert;

    @Id
    @Column(name = "DATE_TIME", nullable = false)
    private LocalDate dateTime;

    @Id
    @Column(name = "SEAT_ROW", nullable = false)
    private SeatRow seatRow;

    @Id
    @Column(name = "SEAT_NUMBER", nullable = false)
    @Convert(converter = SeatNumberConverter.class)
    private SeatNumber seatNumber;

    @ManyToOne
    @Column(name = "RESERVATION")
    private Reservation reservation;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private SeatStatus seatStatus = SeatStatus.FREE;

    @Column(name = "PRICE_BAND", nullable = false)
    @Enumerated(EnumType.STRING)
    private PriceBand priceBand;

    public Seat() {}

    public Seat(Concert concert, LocalDate dateTime, SeatRow seatRow, SeatNumber seatNumber, Reservation reservation, SeatStatus seatStatus) {
        this.concert = concert;
        this.dateTime = dateTime;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
        this.reservation = reservation;
        this.seatStatus = seatStatus;

        this.priceBand = this.determinePriceBand();
    }

    public Concert getConcert() {
        return concert;
    }

    public void setConcert(Concert concert) {
        this.concert = concert;
    }

    public LocalDate getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDate dateTime) {
        this.dateTime = dateTime;
    }

    public SeatRow getSeatRow() {
        return seatRow;
    }

    public void setSeatRow(SeatRow seatRow) {
        this.seatRow = seatRow;
    }

    public SeatNumber getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(SeatNumber seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public SeatStatus getSeatStatus() {
        return seatStatus;
    }

    public void setSeatStatus(SeatStatus seatStatus) {
        this.seatStatus = seatStatus;
    }

    public PriceBand getPriceBand() {
        return priceBand;
    }

    public void setPriceBand(PriceBand priceBand) {
        this.priceBand = priceBand;
    }

    /**
     * Helper method used for determining price band based on seat row.
     *
     * @return Corresponding price band to this seats row
     */
    private PriceBand determinePriceBand() {
        switch (this.seatRow) {
            case A:
                return PriceBand.PriceBandB;
            case B:
                return PriceBand.PriceBandB;
            case C:
                return PriceBand.PriceBandB;
            case D:
                return PriceBand.PriceBandB;
            case E:
                return PriceBand.PriceBandA;
            case F:
                return PriceBand.PriceBandA;
            case G:
                return PriceBand.PriceBandA;
            case H:
                return PriceBand.PriceBandC;
            case I:
                return PriceBand.PriceBandA;
            case J:
                return PriceBand.PriceBandA;
            case K:
                return PriceBand.PriceBandA;
            case L:
                return PriceBand.PriceBandA;
            case M:
                return PriceBand.PriceBandA;
            case N:
                return PriceBand.PriceBandC;
            case O:
                return PriceBand.PriceBandC;
            case P:
                return PriceBand.PriceBandC;
            case Q:
                return PriceBand.PriceBandC;
            case R:
                return PriceBand.PriceBandC;
        }

        // Return null if row was invalid
        return null;
    }
}
