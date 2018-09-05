package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.common.types.SeatStatus;
import nz.ac.auckland.concert.service.domain.jpa.LocalDateTimeConverter;
import nz.ac.auckland.concert.service.domain.jpa.SeatNumberConverter;
import nz.ac.auckland.concert.utility.SeatUtility;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "SEATS")
public class Seat implements Serializable {

    @Id
    @ManyToOne
    private Concert concert;

    @Id
    @Column(name = "DATE_TIME", nullable = false)
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime dateTime;

    @Id
    @Column(name = "SEAT_ROW", nullable = false)
    private SeatRow seatRow;

    @Id
    @Column(name = "SEAT_NUMBER", nullable = false)
    @Convert(converter = SeatNumberConverter.class)
    private SeatNumber seatNumber;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private SeatStatus seatStatus = SeatStatus.FREE;

    @Column(name = "PRICE_BAND", nullable = false)
    @Enumerated(EnumType.STRING)
    private PriceBand priceBand;

    @Column(name = "TIME_STAMP")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime timeStamp;

    @ManyToOne
    private Reservation _reservation;

    public Seat() {}

    public Seat(Concert concert, LocalDateTime dateTime, SeatRow seatRow, SeatNumber seatNumber) {
        this.concert = concert;
        this.dateTime = dateTime;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;

        this.priceBand = SeatUtility.determinePriceBand(this.seatRow);
    }

    public Concert getConcert() {
        return concert;
    }

    public void setConcert(Concert concert) {
        this.concert = concert;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
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

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }
}
