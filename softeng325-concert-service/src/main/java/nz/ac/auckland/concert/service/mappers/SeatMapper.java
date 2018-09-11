package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.service.domain.Seat;

public class SeatMapper {
    public static SeatDTO toDTO(Seat seat) {
        return new SeatDTO(
                seat.getSeatRow(),
                seat.getSeatNumber()
        );
    }
}
