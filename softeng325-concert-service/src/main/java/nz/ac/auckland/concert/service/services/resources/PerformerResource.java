package nz.ac.auckland.concert.service.services.resources;

import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.service.domain.Performer;
import nz.ac.auckland.concert.service.mappers.PerformerMapper;
import nz.ac.auckland.concert.service.services.PersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/performers")
public class PerformerResource implements SubscriptionResource<PerformerDTO>{
    private PersistenceManager _persistenceManager;

    private static Logger _logger = LoggerFactory
            .getLogger(PerformerResource.class);

    public PerformerResource() {
        _persistenceManager = PersistenceManager.instance();
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response createPerformer(PerformerDTO performerDTO) {

        this.process(performerDTO);
        return null;
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getPerformers() {
        EntityManager em = _persistenceManager.createEntityManager();

        try {
            _logger.debug("Retrieving all performers...");

            em.getTransaction().begin();

            List<Performer> performers = em.createQuery("SELECT p FROM Performer p", Performer.class).getResultList();

            em.getTransaction().commit();

            List<PerformerDTO> performerDTOs;

            performerDTOs = performers.stream().map(PerformerMapper::toDTO).collect(Collectors.toList());

            GenericEntity<List<PerformerDTO>> entity = new GenericEntity<List<PerformerDTO>>(performerDTOs) {
            };

            _logger.debug("Successfully retrieved performers");
            return Response.ok(entity).build();
        } catch (Exception e) {
            return Response.serverError().entity(Messages.SERVICE_COMMUNICATION_ERROR).build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getPerformer(@PathParam("id") long id) {
        EntityManager em = _persistenceManager.createEntityManager();

        try {
            _logger.debug("Retrieving performer with id: " + id + "...");

            em.getTransaction().begin();

            Performer performer = em.find(Performer.class, id);

            em.getTransaction().commit();

            if (performer == null) {
                _logger.debug("Could not find performer with id " + id);
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            PerformerDTO performerDTO = PerformerMapper.toDTO(performer);

            _logger.debug("Retrieved performer with id: " + id);
            return Response.ok(performerDTO).build();
        } catch (Exception e) {
            return Response.serverError().entity(Messages.SERVICE_COMMUNICATION_ERROR).build();
        } finally {
            em.close();
        }
    }

    @Override
    public Response unsubscribe(Cookie authToken) {
        return null;
    }

    @Override
    public Response subscribe(AsyncResponse response, Cookie authToken) {
        return null;
    }

    @Override
    public void process(PerformerDTO performerDTO) {

    }
}
