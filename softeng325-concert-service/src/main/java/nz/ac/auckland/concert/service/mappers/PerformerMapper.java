package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.service.domain.Performer;

public class PerformerMapper {

    public static PerformerDTO toDTO(Performer performer) {
        return new PerformerDTO(
                performer.getId(),
                performer.getName(),
                performer.getImageName(),
                performer.getGenre(),
                performer.getConcertIds()
        );
    }
}
