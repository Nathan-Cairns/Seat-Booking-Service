package nz.ac.auckland.concert.service.services.resources;

import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.common.types.SeatStatus;
import nz.ac.auckland.concert.service.domain.Concert;
import nz.ac.auckland.concert.service.domain.Reservation;
import nz.ac.auckland.concert.service.domain.Seat;
import nz.ac.auckland.concert.service.domain.User;
import nz.ac.auckland.concert.service.mappers.ReservationMapper;
import nz.ac.auckland.concert.service.mappers.SeatMapper;
import nz.ac.auckland.concert.service.services.PersistenceManager;
import nz.ac.auckland.concert.service.util.TheatreUtility;
import nz.ac.auckland.concert.utility.TheatreLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/reservations")
public class ReservationResource {

    private static final Response MISSING_FIELD_RESPONSE = Response
            .status(Response.Status.BAD_REQUEST)
            .entity(Messages.RESERVATION_REQUEST_WITH_MISSING_FIELDS)
            .build();

    private PersistenceManager _persistenceManager;

    private Logger _logger = LoggerFactory
            .getLogger(ReservationResource.class);

    public ReservationResource() {
        _persistenceManager = PersistenceManager.instance();
    }

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

            em.getTransaction().commit();

            // Check and update seats
            em.getTransaction().begin();

            List<Seat> seats = em.createQuery("SELECT s from Seat s WHERE s.concert.id = :cid " +
                    "AND s.dateTime = :date", Seat.class)
                    .setParameter("cid", concert.getId())
                    .setParameter("date", reservationRequestDTO.getDate())
                    .setLockMode(LockModeType.OPTIMISTIC)
                    .getResultList();

            // If no seats exist for concert create them
            if (seats.isEmpty()) {
                for (SeatRow seatRow : SeatRow.values()) {
                    for (int i = 1; i <= TheatreLayout.getNumberOfSeatsForRow(seatRow); i++) {
                        Seat seat = new Seat(
                                concert,
                                reservationRequestDTO.getDate(),
                                seatRow,
                                new SeatNumber(i));
                        em.persist(seat);
                    }
                }
            } else {
                // TODO
                // Free seats which have been held for too long???
            }

            em.getTransaction().commit();

            // Make reservation
            em.getTransaction().begin();

            // Get list of all not available seats
            List<Seat> unavailableSeats = em.createQuery("SELECT s FROM Seat s WHERE s.concert.id = :cid AND " +
                    "s.dateTime=:date AND s.priceBand = :priceBand AND s.seatStatus = :status " +
                    "AND s.seatStatus = :status2", Seat.class)
                    .setParameter("cid", concert.getId())
                    .setParameter("date", reservationRequestDTO.getDate())
                    .setParameter("priceBand", reservationRequestDTO.getSeatType())
                    .setParameter("status", SeatStatus.BOOKED)
                    .setParameter("status2", SeatStatus.PENDING)
                    .setLockMode(LockModeType.OPTIMISTIC)
                    .getResultList();

            // Convert unavailable seats to DTO
            Set<SeatDTO> unavailableSeatDTOList =
                    unavailableSeats.stream().map(SeatMapper::toDTO).collect(Collectors.toSet());

            // Get available seats
            Set<SeatDTO> availableSeats = TheatreUtility.findAvailableSeats(
                    reservationRequestDTO.getNumberOfSeats(),
                    reservationRequestDTO.getSeatType(),
                    unavailableSeatDTOList);

            // Check if enough seats are available
            if (availableSeats.isEmpty() || availableSeats.size() < reservationRequestDTO.getNumberOfSeats()) {
                return Response
                        .status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .entity(Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION)
                        .build();
            }
            // Set status to pending for required amount of seats
            Set<Seat> pendingSeats = new HashSet<>();
            int i = 0;
            for (SeatDTO s : availableSeats) {
                Seat seatToReserve = em.createQuery("SELECT s FROM Seat s WHERE s.concert.id = :cid " +
                        "AND s.dateTime = :date AND s.seatRow = :row AND s.seatNumber = :number", Seat.class)
                        .setParameter("cid", concert.getId())
                        .setParameter("date", reservationRequestDTO.getDate())
                        .setParameter("row", s.getRow())
                        .setParameter("number", s.getNumber())
                        .getSingleResult();

                seatToReserve.setSeatStatus(SeatStatus.PENDING);
                em.persist(seatToReserve);

                pendingSeats.add(seatToReserve);

                i++;
                if (i == reservationRequestDTO.getNumberOfSeats() - 1)
                    break;
            }

            Reservation reservation = new Reservation(
                    user,
                    concert,
                    reservationRequestDTO.getSeatType(),
                    pendingSeats
            );

            em.persist(reservation);
            em.getTransaction().commit();

            ReservationDTO reservationDTO = ReservationMapper.toDTO(reservation, reservationRequestDTO);
            return Response
                    .ok(reservationDTO)
                    .build();
        } catch (Exception e) {
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }
}
