package nz.ac.auckland.concert.service.services.resources;

import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.service.domain.User;
import nz.ac.auckland.concert.service.services.PersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/reservations")
public class ReservationResource {
    private PersistenceManager _perPersistenceManager;

    private Logger _logger = LoggerFactory
            .getLogger(ReservationResource.class);

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response requestReservation(ReservationRequestDTO reservationRequestDTO,
                                       @CookieParam("AuthToken") Cookie authToken) {
        EntityManager em = _perPersistenceManager.createEntityManager();

        try {
            em.getTransaction().begin();
            // Check auth token is not null
            if (authToken == null) {
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity(Messages.UNAUTHENTICATED_REQUEST)
                        .build();
            }

            // Check there is a user with auth token
            User user = em.createQuery("SELECT u from User u WHERE u.TOKEN = :token", User.class)
                    .setParameter("token", authToken).getSingleResult();

            if (user == null) {
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity(Messages.UNAUTHENTICATED_REQUEST)
                        .build();
            }

            // TODO more checks

            // TODO make the reservation

            // TODO implement reservation domain object

            em.getTransaction().commit();
            // TODO return response with resrvation dto object
            return null;
        } catch (Exception e) {
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }
}
