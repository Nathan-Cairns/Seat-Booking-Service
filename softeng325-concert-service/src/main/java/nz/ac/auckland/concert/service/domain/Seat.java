package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.common.types.SeatStatus;

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
    private SeatNumber seatNumber;

    @ManyToOne
    @Column(name = "RESERVATION")
    private Reservation reservation;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private SeatStatus seatStatus = SeatStatus.FREE;
    
    public Seat() {}

    public Seat(Concert concert, LocalDate dateTime, SeatRow seatRow, SeatNumber seatNumber, Reservation reservation, SeatStatus seatStatus) {
        this.concert = concert;
        this.dateTime = dateTime;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
        this.reservation = reservation;
        this.seatStatus = seatStatus;
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
}
