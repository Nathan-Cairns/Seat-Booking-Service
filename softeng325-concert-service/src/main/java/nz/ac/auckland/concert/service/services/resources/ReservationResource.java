package nz.ac.auckland.concert.service.services.resources;

import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.common.types.SeatStatus;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.Seat;
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
import java.util.List;

@Path("/reservations")
public class ReservationResource {

    private static final Response MISSING_FIELD_RESPONSE = Response
            .status(Response.Status.BAD_REQUEST)
            .entity(Messages.RESERVATION_REQUEST_WITH_MISSING_FIELDS)
            .build();

    private PersistenceManager _persistenceManager;

    private Logger _logger = LoggerFactory
            .getLogger(ReservationResource.class);

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response requestReservation(ReservationRequestDTO reservationRequestDTO,
                                       @CookieParam("AuthToken") Cookie authToken) {
        EntityManager em = _persistenceManager.createEntityManager();

        try {
            em.getTransaction().begin();
            // Check auth token is not null
            if (authToken == null) {
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity(Messages.UNAUTHENTICATED_REQUEST)
                        .build();
            }

            // Get corresponding concert
            Concert concert = em.find(Concert.class, reservationRequestDTO.getConcertId());

            // Check there is a user with auth token
            User user = em.createQuery("SELECT u from User u WHERE u.authToken = :token", User.class)
                    .setParameter("token", authToken).getSingleResult();

            if (user == null) {
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity(Messages.BAD_AUTHENTICATON_TOKEN)
                        .build();
            }

            // Check required fields
            if (reservationRequestDTO.getNumberOfSeats() == 0) {
                return MISSING_FIELD_RESPONSE;
            }
            if (reservationRequestDTO.getConcertId() == null) {
                return MISSING_FIELD_RESPONSE;
            }
            if (reservationRequestDTO.getDate() == null) {
                return MISSING_FIELD_RESPONSE;
            }
            if (reservationRequestDTO.getSeatType() == null) {
                return MISSING_FIELD_RESPONSE;
            }

            // Check valid date
            if (!concert.getDates().contains(reservationRequestDTO.getDate())) {
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE)
                        .build();
            }

            // Get list of available seats
            List<Seat> seats = em.createQuery("SELECT s FROM Seat s WHERE s.concert.id = :cid AND " +
                    "s.dateTime=:date AND s.priceBand = :priceBand AND s.seatStatus = :status", Seat.class)
                    .setParameter("cid", concert.getId())
                    .setParameter("date", reservationRequestDTO.getDate())
                    .setParameter("priceBand", reservationRequestDTO.getSeatType())
                    .setParameter("status", SeatStatus.FREE)
                    .getResultList();

            // Check number of seats available
            if (seats.isEmpty() || seats.size() < reservationRequestDTO.getNumberOfSeats()) {
                return Response
                        .status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .entity(Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION)
                        .build();
            }

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
