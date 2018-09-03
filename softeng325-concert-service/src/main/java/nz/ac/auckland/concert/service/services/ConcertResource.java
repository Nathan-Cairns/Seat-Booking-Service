package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.service.domain.Concert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NamedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/concerts")
@NamedQuery(name="Concerts.findAll", query="SELECT c FROM CONCERTS c")
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
        _logger.debug("Retrieving all concerts");

        EntityManager em = _persistenceManager.createEntityManager();

        em.getTransaction().begin();

        List<Concert> concerts = em.createNamedQuery("Concerts.findAll", Concert.class).getResultList();

        if (concerts == null || concerts.isEmpty()) {
            _logger.debug("No concerts were found");
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        _logger.debug("Successfully retrieved all concerts");
        return Response.status(Response.Status.OK).entity(concerts).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getConcert(@PathParam("id") long id) {
        _logger.debug("Retrieving Concert with id: " + id + "...");
        EntityManager em = _persistenceManager.createEntityManager();

        em.getTransaction().begin();

        Concert concert = em.find(Concert.class, id);

        if (concert == null) {
            _logger.debug("Could not find concert with id: " + id);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ConcertDTO concertDTO = new ConcertDTO(concert.getId(), concert.getTitle(), concert.getDates(),
                concert.getTariff(), concert.getPerformerIds());

        _logger.debug("Successfully found concert with id: " + id);
        return Response.status(Response.Status.OK).entity(concertDTO).build();
    }

}
