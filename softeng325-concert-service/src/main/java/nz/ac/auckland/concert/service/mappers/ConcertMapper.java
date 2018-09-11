package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.service.domain.Concert;

public class ConcertMapper {
    public static ConcertDTO toDTO(Concert concert) {
        return new ConcertDTO(
                concert.getId(),
                concert.getTitle(),
                concert.getDates(),
                concert.getTariff(),
                concert.getPerformerIds()
        );
    }
}
