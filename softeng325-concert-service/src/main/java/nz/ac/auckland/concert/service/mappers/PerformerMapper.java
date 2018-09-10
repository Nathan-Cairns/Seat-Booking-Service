package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.Performer;
import nz.ac.auckland.concert.service.services.PersistenceManager;

import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.Set;

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

    public static Performer toDomain(PerformerDTO performerDTO) {
        EntityManager em = PersistenceManager.instance().createEntityManager();

        em.getTransaction().begin();

        Set<Concert> concerts = new HashSet<>();

        for (long id : performerDTO.getConcertIds()) {
            concerts.add(em.find(Concert.class, id));
        }

        em.getTransaction().commit();
        em.close();
        return new Performer(
                performerDTO.get_genre(),
                performerDTO.getImageName(),
                performerDTO.getName(),
                concerts
        );
    }
}
