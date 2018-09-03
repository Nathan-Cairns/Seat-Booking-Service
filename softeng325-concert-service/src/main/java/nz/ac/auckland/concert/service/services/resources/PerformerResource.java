package nz.ac.auckland.concert.service.services.resources;

import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.service.domain.Performer;
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

@Path("/performers")
public class PerformerResource {
    private PersistenceManager _persistenceManager;

    private static Logger _logger = LoggerFactory
            .getLogger(PerformerResource.class);

    public PerformerResource() {
        _persistenceManager = PersistenceManager.instance();
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getPerformers() {
        try {
            _logger.debug("Retrieving all performers...");

            EntityManager em = _persistenceManager.createEntityManager();

            em.getTransaction().begin();

            List<Performer> performers = em.createQuery("SELECT p FROM Performer p", Performer.class).getResultList();

            List<PerformerDTO> performerDTOs;

            performerDTOs = performers.stream().map(performer -> new PerformerDTO(performer.getId(),
                    performer.getName(), performer.getImageName(), performer.getGenre(),
                    performer.getConcertIds())).collect(Collectors.toList());

            GenericEntity<List<PerformerDTO>> entity = new GenericEntity<List<PerformerDTO>>(performerDTOs) {
            };

            _logger.debug("Successfully retrieved performers");
            return Response.ok(entity).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getPerformer(@PathParam("id") long id) {
        try {
            _logger.debug("Retrieving performer with id: " + id + "...");

            EntityManager em = _persistenceManager.createEntityManager();

            em.getTransaction().begin();

            Performer performer = em.find(Performer.class, id);

            if (performer == null) {
                _logger.debug("Could not find performer with id " + id);
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            PerformerDTO performerDTO = new PerformerDTO(performer.getId(), performer.getName(), performer.getImageName(),
                    performer.getGenre(), performer.getConcertIds());

            _logger.debug("Retrieved performer with id: " + id);
            return Response.ok(performerDTO).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }
}
