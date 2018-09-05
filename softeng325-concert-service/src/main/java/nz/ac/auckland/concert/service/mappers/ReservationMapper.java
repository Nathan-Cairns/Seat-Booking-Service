package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.service.domain.Reservation;
import nz.ac.auckland.concert.service.domain.Seat;

import java.util.Set;
import java.util.stream.Collectors;

public class ReservationMapper {

    public static ReservationDTO toDTO(Reservation reservation, ReservationRequestDTO reservationRequestDTO) {
        return new ReservationDTO(
                reservation.getId(),
                reservationRequestDTO,
                generateDTOSeats(reservation.getSeats())
        );
    }

    private static Set<SeatDTO> generateDTOSeats(Set<Seat> seats) {
        return seats.stream().map(SeatMapper::toDTO).collect(Collectors.toSet());
    }
}
