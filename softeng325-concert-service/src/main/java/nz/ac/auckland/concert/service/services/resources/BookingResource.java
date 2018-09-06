package nz.ac.auckland.concert.service.services.resources;

import nz.ac.auckland.concert.common.dto.BookingDTO;
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
import nz.ac.auckland.concert.utility.SeatUtility;
import nz.ac.auckland.concert.utility.TheatreLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/bookings")
public class BookingResource {

    private static final Response MISSING_FIELD_RESPONSE = Response
            .status(Response.Status.BAD_REQUEST)
            .entity(Messages.RESERVATION_REQUEST_WITH_MISSING_FIELDS)
            .build();

    private PersistenceManager _persistenceManager;

    private Logger _logger = LoggerFactory
            .getLogger(BookingResource.class);

    public BookingResource() {
        _persistenceManager = PersistenceManager.instance();
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getBookings(@CookieParam("AuthToken") Cookie authToken) {
        EntityManager em = _persistenceManager.createEntityManager();

        try {
            em.getTransaction().begin();
            // Check auth token is not null
            if (authToken == null) {
                _logger.debug("No auth token");
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity(Messages.UNAUTHENTICATED_REQUEST)
                        .build();
            }

            // Check there is a user with auth token
            User user = em.createQuery("SELECT u from User u WHERE u.authToken = :token", User.class)
                    .setParameter("token", authToken.getValue()).getSingleResult();

            if (user == null) {
                _logger.debug("No user corresponding to auth token");
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity(Messages.BAD_AUTHENTICATON_TOKEN)
                        .build();
            }

            List<Reservation> reservations = em
                    .createQuery("SELECT r FROM Reservation r WHERE r.user.username = :username", Reservation.class)
                    .setParameter("username", user.getUsername())
                    .getResultList();

            Set<BookingDTO> bookingDTOS = reservations.stream().filter(Reservation::getConfirmed)
                    .map(ReservationMapper::reservationDomainToBookingDTO).collect(Collectors.toSet());

            GenericEntity<Set<BookingDTO>> genericEntity = new GenericEntity<Set<BookingDTO>>(bookingDTOS) {};

            em.getTransaction().commit();

            return Response
                    .ok(genericEntity)
                    .build();
        } catch (Exception e) {
            return Response.serverError().entity(Messages.SERVICE_COMMUNICATION_ERROR).build();
        } finally {
            em.close();
        }
    }

    @POST
    @Path("/reserve")
    @Consumes(MediaType.APPLICATION_XML)
    public Response requestReservation(ReservationRequestDTO reservationRequestDTO,
                                       @CookieParam("AuthToken") Cookie authToken) {
        EntityManager em = _persistenceManager.createEntityManager();

        try {
            em.getTransaction().begin();
            // Check auth token is not null
            if (authToken == null) {
                _logger.debug("No auth token");
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity(Messages.UNAUTHENTICATED_REQUEST)
                        .build();
            }

            // Get corresponding concert
            Concert concert = em.find(Concert.class, reservationRequestDTO.getConcertId());

            // Check there is a user with auth token
            User user = em.createQuery("SELECT u from User u WHERE u.authToken = :token", User.class)
                    .setParameter("token", authToken.getValue()).getSingleResult();

            if (user == null) {
                _logger.debug("No user corresponding to auth token");
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity(Messages.BAD_AUTHENTICATON_TOKEN)
                        .build();
            }

            // Check required fields
            if (reservationRequestDTO.getNumberOfSeats() == 0) {
                _logger.debug("Missing field: seats");
                return MISSING_FIELD_RESPONSE;
            }
            if (reservationRequestDTO.getConcertId() == null) {
                _logger.debug("Missing field: concert id");
                return MISSING_FIELD_RESPONSE;
            }
            if (reservationRequestDTO.getDate() == null) {
                _logger.debug("Missing field: date");
                return MISSING_FIELD_RESPONSE;
            }
            if (reservationRequestDTO.getSeatType() == null) {
                _logger.debug("Missing field: seat type");
                return MISSING_FIELD_RESPONSE;
            }

            // Check valid date
            if (!concert.getDates().contains(reservationRequestDTO.getDate())) {
                _logger.debug("Invalid date");
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE)
                        .build();
            }

            em.getTransaction().commit();

            this.initSeats(concert, reservationRequestDTO);

            ReservationDTO reservationDTO = this.reserveSeatsForRequest(concert, reservationRequestDTO, user);

            if (reservationDTO == null) {
                return Response
                        .status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .entity(Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION)
                        .build();
            }
            return Response
                    .accepted(reservationDTO)
                    .build();
        } catch (Exception e) {
            return Response.serverError().entity(Messages.SERVICE_COMMUNICATION_ERROR).build();
        } finally {
            em.close();
        }
    }

    @POST
    @Path("/book")
    @Consumes(MediaType.APPLICATION_XML)
    public Response makeReservation(ReservationDTO reservationDTO,
                                    @CookieParam("AuthToken") Cookie authToken) {
        EntityManager em = _persistenceManager.createEntityManager();

        _logger.debug("Confirming booking for reservation " + reservationDTO.getId());

        try {

            em.getTransaction().begin();

            // Check there is an auth token
            if (authToken == null) {
                _logger.debug("No auth token");
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity(Messages.UNAUTHENTICATED_REQUEST)
                        .build();
            }

            // Check there is a user with auth token
            User user = em.createQuery("SELECT u from User u WHERE u.authToken = :token", User.class)
                    .setParameter("token", authToken.getValue()).getSingleResult();

            if (user == null) {
                _logger.debug("No user corresponding to auth token");
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity(Messages.BAD_AUTHENTICATON_TOKEN)
                        .build();
            }

            // Doesn't have a credit card
            if (user.getCreditCard() == null) {
                return Response
                        .status(Response.Status.PAYMENT_REQUIRED)
                        .entity(Messages.CREDIT_CARD_NOT_REGISTERED)
                        .build();
            }

            Reservation reservation = em.find(Reservation.class, reservationDTO.getId());

            // Tried to get a reservation which doesnt belong to them
            if (!reservation.getUser().getUsername().equals(user.getUsername())) {
                _logger.debug("Reservation does not belong to the user!");
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity(Messages.BAD_AUTHENTICATON_TOKEN)
                        .build();
            }

            List<Seat> seats = new ArrayList<>();


            _logger.debug("Booking " + reservation.getSeats().size() + " seats");
            for (Seat seat : reservation.getSeats()) {
                if (!LocalDateTime.now().isAfter(seat.getTimeStamp()
                        .plusSeconds(SeatUtility.RESERVATION_EXPIRY_TIME_IN_SECONDS))) {
                    _logger.debug("Booking seat " + seat.getSeatNumber() + seat.getSeatRow());
                    seat.setSeatStatus(SeatStatus.BOOKED);
                    seats.add(seat);
                } else {
                    _logger.debug("Seat res timed out!");
                    return Response
                            .status(Response.Status.REQUEST_TIMEOUT)
                            .entity(Messages.EXPIRED_RESERVATION)
                            .build();
                }
            }

            // Merge the booked seats
            for (Seat s : seats) {
                em.merge(s);
            }

            // Merge confirmed reservation
            reservation.setConfirmed(true);
            em.merge(reservation);

            em.getTransaction().commit();

            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().entity(Messages.SERVICE_COMMUNICATION_ERROR).build();
        } finally {
            em.close();
        }
    }

    private void bookSeats() {
        EntityManager em = _persistenceManager.createEntityManager();

        try {

        } catch (OptimisticLockException e) {
            this.bookSeats();
        } finally {
            em.close();
        }
    }

    /**
     * Places reservations on some seats
     *
     * @return Null if not seats could be booked due to insufficient seats, Otherwise returns a reservationDTO object
     *      representing the reservation just made.
     */
    private ReservationDTO reserveSeatsForRequest(Concert concert, ReservationRequestDTO reservationRequestDTO, User user) {
        ReservationDTO reservationDTO = null;
        EntityManager em = _persistenceManager.createEntityManager();

        try {
            em.getTransaction().begin();

            // Get list of all not available seats
            _logger.debug("Get unavailable seats");
            List<Seat> unavailableSeats = em.createQuery("SELECT s FROM Seat s WHERE s.concert.id = :cid AND " +
                    "s.dateTime=:date AND s.priceBand = :priceBand AND s.seatStatus = :status " +
                    "OR s.seatStatus = :status2", Seat.class)
                    .setParameter("cid", concert.getId())
                    .setParameter("date", reservationRequestDTO.getDate())
                    .setParameter("priceBand", reservationRequestDTO.getSeatType())
                    .setParameter("status", SeatStatus.BOOKED)
                    .setParameter("status2", SeatStatus.PENDING)
                    .setLockMode(LockModeType.OPTIMISTIC)
                    .getResultList();

            _logger.debug(unavailableSeats.size() + " unavailable seats");

            // Convert unavailable seats to DTO
            Set<SeatDTO> unavailableSeatDTOList =
                    unavailableSeats.stream().map(SeatMapper::toDTO).collect(Collectors.toSet());

            _logger.debug("Size of dto unavailable seats: " + unavailableSeatDTOList.size());

            // Get available seats
            Set<SeatDTO> availableSeats = TheatreUtility.findAvailableSeats(
                    reservationRequestDTO.getNumberOfSeats(),
                    reservationRequestDTO.getSeatType(),
                    unavailableSeatDTOList);

            // Check if enough seats are available
            if (availableSeats.isEmpty() || availableSeats.size() < reservationRequestDTO.getNumberOfSeats()) {
                _logger.debug("Not enough seats available");
                return null;
            }
            // Set status to pending for required amount of seats
            _logger.debug("Retrieving " + reservationRequestDTO.getNumberOfSeats() + " seats");
            _logger.debug(availableSeats.size() + " available seats");
            Set<Seat> pendingSeats = new HashSet<>();
            for (SeatDTO s : availableSeats) {
                _logger.debug("Adding pending seat with row " + s.getRow() + " and number " + s.getNumber());
                Seat seatToReserve = em.createQuery("SELECT s FROM Seat s WHERE s.concert.id = :cid " +
                        "AND s.dateTime = :date AND s.seatRow = :row AND s.seatNumber = :number", Seat.class)
                        .setParameter("cid", concert.getId())
                        .setParameter("date", reservationRequestDTO.getDate())
                        .setParameter("row", s.getRow())
                        .setParameter("number", s.getNumber())
                        .getSingleResult();

                seatToReserve.setTimeStamp(LocalDateTime.now());
                seatToReserve.setSeatStatus(SeatStatus.PENDING);

                pendingSeats.add(seatToReserve);
                em.merge(seatToReserve);
            }

            _logger.debug(pendingSeats.size() + " Pending seats");

            Reservation reservation = new Reservation(
                    user,
                    concert,
                    reservationRequestDTO.getSeatType(),
                    pendingSeats,
                    reservationRequestDTO.getDate()
            );

            _logger.debug(reservation.getSeats().size() + " Pending seats in res " + reservation.getId());

            em.persist(reservation);

            for (Seat seat : pendingSeats) {
                seat.set_reservation(reservation);
                em.persist(seat);
            }

            reservationDTO = ReservationMapper.toDTO(reservation, reservationRequestDTO);
            em.getTransaction().commit();
        } catch (OptimisticLockException e) {
            // Failed comitting to db cause incorrect seat version
            reservationDTO = this.reserveSeatsForRequest(concert, reservationRequestDTO, user);
        } finally {
            em.close();
        }

        return reservationDTO;
    }

    /**
     * Makes sure seats are initialised
     */
    private void initSeats (Concert concert, ReservationRequestDTO reservationRequestDTO) {
        EntityManager em = _persistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            List<Seat> seats = em.createQuery("SELECT s from Seat s WHERE s.concert.id = :cid " +
                    "AND s.dateTime = :date", Seat.class)
                    .setParameter("cid", concert.getId())
                    .setParameter("date", reservationRequestDTO.getDate())
                    .setLockMode(LockModeType.OPTIMISTIC)
                    .getResultList();

            // If no seats exist for concert create them
            if (seats.isEmpty()) {
                _logger.debug("Setting up seats");
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
                _logger.debug("Freeing expired seats");
                for (Seat s : seats) {
                    if (s.getSeatStatus().equals(SeatStatus.PENDING)) {
                        if (LocalDateTime.now().isAfter(s.getTimeStamp()
                                .plusSeconds(SeatUtility.RESERVATION_EXPIRY_TIME_IN_SECONDS))) {
                            _logger.debug("Freeing up seat: " + s.getSeatNumber() + s.getSeatRow());
                            s.setSeatStatus(SeatStatus.FREE);
                            em.merge(s);
                        }
                    }
                }
            }
            em.getTransaction().commit();
        } catch (OptimisticLockException e) {
            // Failed comitting to db cause incorrect seat version
            this.initSeats(concert, reservationRequestDTO);
        } finally {
            em.close();
        }
    }
}
