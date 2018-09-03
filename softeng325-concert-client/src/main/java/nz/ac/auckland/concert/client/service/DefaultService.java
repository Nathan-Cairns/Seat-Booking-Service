package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.awt.*;
import java.util.Set;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class DefaultService implements ConcertService {
    private static Logger _logger = LoggerFactory
            .getLogger(DefaultService.class);

    private static final String WEB_SERVICE_URI = "http://localhost:10000/services";

    private static final String CONCERT_SERVICE = WEB_SERVICE_URI + "/concerts";
    private static final String PERFORMER_SERVICE = WEB_SERVICE_URI + "/performers";
    private static final String USER_SERVICE = WEB_SERVICE_URI + "/users";

    private static Client _client;


    public DefaultService() {
        _client = ClientBuilder.newClient();
    }

    @Override
    public Set<ConcertDTO> getConcerts() throws ServiceException {
        Response response = null;

        try {
            Builder builder = _client.target(CONCERT_SERVICE).request()
                    .accept(MediaType.APPLICATION_XML);

            _logger.debug("Making concerts get request...");
            response = builder.get();

            Set<ConcertDTO> concertDTOS = response.readEntity(new GenericType<Set<ConcertDTO>>() {
            });

            _logger.debug("Successfully retrieved and unmarshalled concerts from server.");
            response.close();
            return concertDTOS;
        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            assert response != null;
            response.close();
        }
    }

    @Override
    public Set<PerformerDTO> getPerformers() throws ServiceException {
        Response response = null;

        try {
            Builder builder = _client.target(PERFORMER_SERVICE).request()
                    .accept(MediaType.APPLICATION_XML);

            _logger.debug("Making performers get request...");
            response = builder.get();

            Set<PerformerDTO> performerDTOList = response.readEntity(new GenericType<Set<PerformerDTO>>() {
            });

            _logger.debug("Successfully retrieved and unmarshalled performers from server.");
            response.close();
            return performerDTOList;
        } catch (Exception e) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } finally {
            assert response != null;
            response.close();
        }
    }

    @Override
    public UserDTO createUser(UserDTO newUser) throws ServiceException {
        Response response = null;
        try {
            Builder builder = _client.target(USER_SERVICE).request()
                    .accept(MediaType.APPLICATION_XML);

            _logger.debug("Making create user request");

            try {
                response = builder.post(Entity.entity(newUser, MediaType.APPLICATION_XML));
            } catch (Exception e) {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }

            if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                _logger.debug(String.valueOf(response.getStatus()));
                throw new ServiceException(response.readEntity(String.class));
            }

            _logger.debug("User Successfully created at url: " + response.getLocation());
            response.close();
            return new UserDTO(newUser.getUsername(), newUser.getPassword(),
                    newUser.getLastname(), newUser.getFirstname());
        } finally {
            assert response != null;
            response.close();
        }
    }

    @Override
    public UserDTO authenticateUser(UserDTO user) throws ServiceException {
        // Todo
        return null;
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
