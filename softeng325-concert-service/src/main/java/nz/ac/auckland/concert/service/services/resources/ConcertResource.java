package nz.ac.auckland.concert.service.services.resources;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.services.PersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/concerts")
public class ConcertResource {

    private PersistenceManager _persistenceManager;

    private static Logger _logger = LoggerFactory
            .getLogger(ConcertResource.class);

    public ConcertResource() {
        _persistenceManager = PersistenceManager.instance();
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getConcerts() {
        EntityManager em = _persistenceManager.createEntityManager();

        try {
            _logger.debug("Retrieving all concerts");

            em.getTransaction().begin();

            List<Concert> concerts = em.createQuery("SELECT c FROM Concert c", Concert.class).getResultList();

            List<ConcertDTO> concertDTOS = concerts.stream().map(concert -> new ConcertDTO(concert.getId(),
                    concert.getTitle(), concert.getDates(), concert.getTariff(),
                    concert.getPerformerIds())).collect(Collectors.toList());

            GenericEntity<List<ConcertDTO>> entity = new GenericEntity<List<ConcertDTO>>(concertDTOS) {
            };

            _logger.debug("Successfully retrieved all concerts");
            return Response.ok(entity).build();
        } catch (Exception e) {
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getConcert(@PathParam("id") long id) {
        EntityManager em = _persistenceManager.createEntityManager();

        try {
            _logger.debug("Retrieving Concert with id: " + id + "...");

            em.getTransaction().begin();

            Concert concert = em.find(Concert.class, id);

            if (concert == null) {
                _logger.debug("Could not find concert with id: " + id);
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            ConcertDTO concertDTO = new ConcertDTO(concert.getId(), concert.getTitle(), concert.getDates(),
                    concert.getTariff(), concert.getPerformerIds());

            _logger.debug("Successfully found concert with id: " + id);
            return Response.ok(concertDTO).build();
        } catch (Exception e) {
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }

}
