package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.service.mappers.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Set;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class DefaultService implements ConcertService {

    private static final String WEB_SERVICE_URI = "http://localhost:10000/services";

    private static final String CONCERT_SERVICE = WEB_SERVICE_URI + "/concerts";
    private static final String PERFORMER_SERVICE = WEB_SERVICE_URI + "/performers";
    private static final String USER_SERVICE = WEB_SERVICE_URI + "/users";
    private static final String AUTH_SERVICE = USER_SERVICE + "/auth";

    private static Logger _logger = LoggerFactory
            .getLogger(DefaultService.class);

    private Cookie _authToken;

    @Override
    public Set<ConcertDTO> getConcerts() throws ServiceException {
        Response response;
        Client client = ClientBuilder.newClient();

        try {
            Builder builder = client.target(CONCERT_SERVICE).request()
                    .accept(MediaType.APPLICATION_XML);

            _logger.debug("Making concerts get request...");
            response = builder.get();

            Set<ConcertDTO> concertDTOS = response.readEntity(new GenericType<Set<ConcertDTO>>() {
            });

            _logger.debug("Successfully retrieved and unmarshalled concerts from server.");
            return concertDTOS;
        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            client.close();
        }
    }

    @Override
    public Set<PerformerDTO> getPerformers() throws ServiceException {
        Client client = ClientBuilder.newClient();
        Response response;

        try {
            Builder builder = client.target(PERFORMER_SERVICE).request()
                    .accept(MediaType.APPLICATION_XML);

            _logger.debug("Making performers get request...");
            response = builder.get();

            Set<PerformerDTO> performerDTOList = response.readEntity(new GenericType<Set<PerformerDTO>>() {
            });

            _logger.debug("Successfully retrieved and unmarshalled performers from server.");
            return performerDTOList;
        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            client.close();
        }
    }

    @Override
    public UserDTO createUser(UserDTO newUser) throws ServiceException {
        Client client = ClientBuilder.newClient();
        Response response;

        try {
            Builder builder = client.target(USER_SERVICE).request()
                    .accept(MediaType.APPLICATION_XML);

            _logger.debug("Making create user request");

            response = builder.post(Entity.entity(newUser, MediaType.APPLICATION_XML));

            if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                _logger.debug(String.valueOf(response.getStatus()));
                throw new ServiceException(response.readEntity(String.class));
            }

            _authToken = (Cookie) response.getCookies().values().toArray()[0];
            _logger.debug("Auth token set to: " + _authToken);

            _logger.debug("User Successfully created at url: " + response.getLocation());
            return new UserDTO(newUser.getUsername(), newUser.getPassword(),
                    newUser.getLastname(), newUser.getFirstname());
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                throw e;
            } else {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        }finally {
            client.close();
        }
    }

    @Override
    public UserDTO authenticateUser(UserDTO user) throws ServiceException {
        Client client = ClientBuilder.newClient();
        Response response;

        try {
            Builder builder = client.target(AUTH_SERVICE).request()
                    .accept(MediaType.APPLICATION_XML);

            _logger.debug("Making auth request");

            try {
                response = builder.put(Entity.entity(user, MediaType.APPLICATION_XML));
            } catch (Exception e) {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }

            if (response.getStatus() != Response.Status.ACCEPTED.getStatusCode()) {
                _logger.debug(String.valueOf(response.getStatus()));
                throw new ServiceException(response.readEntity(String.class));
            }

            _authToken = (Cookie) response.getCookies().values().toArray()[0];
            _logger.debug("Auth token set to: " + _authToken);

            _logger.debug("User successfully authenticated!");
            return response.readEntity(UserDTO.class);
        } finally {
            client.close();
        }
    }

    @Override
    public Image getImageForPerformer(PerformerDTO performer) throws ServiceException {
        // Todo
        return null;
    }

    @Override
    public ReservationDTO reserveSeats(ReservationRequestDTO reservationRequest) throws ServiceException {
        // Todo
        return null;
    }

    @Override
    public void confirmReservation(ReservationDTO reservation) throws ServiceException {
        // Todo
    }

    @Override
    public void registerCreditCard(CreditCardDTO creditCard) throws ServiceException {
        // Todo
    }

    @Override
    public Set<BookingDTO> getBookings() throws ServiceException {
        return null;
    }
}
