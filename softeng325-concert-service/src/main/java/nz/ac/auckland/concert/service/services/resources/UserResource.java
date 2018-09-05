package nz.ac.auckland.concert.service.services.resources;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.service.domain.CreditCard;
import nz.ac.auckland.concert.service.domain.User;
import nz.ac.auckland.concert.service.mappers.CreditCardMapper;
import nz.ac.auckland.concert.service.mappers.UserMapper;
import nz.ac.auckland.concert.service.services.PersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/users")
public class UserResource {
    private PersistenceManager _persistenceManager;

    private static Logger _logger = LoggerFactory
            .getLogger(UserResource.class);

    public UserResource() {
        _persistenceManager = PersistenceManager.instance();
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getUser(@PathParam("id") long id) {
        EntityManager em = _persistenceManager.createEntityManager();

        try {
            _logger.debug("Retrieving user with id " + id);

            em.getTransaction().begin();

            User user = em.find(User.class, id);

            em.getTransaction().commit();

            if (user == null) {
                _logger.debug("Could not find user with id: " + id);
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            UserDTO userDTO = UserMapper.toDTO(user);

            _logger.debug("Retrieved user with id: " + id);
            return Response.ok(userDTO).build();
        } catch (Exception e) {
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response postUser(UserDTO userDTO){
        EntityManager em = _persistenceManager.createEntityManager();

        try {
            _logger.debug("Creating user: " + userDTO.getUsername());

            em.getTransaction().begin();

            List<String> required = new ArrayList<>();
            required.add(userDTO.getUsername());
            required.add(userDTO.getPassword());
            required.add(userDTO.getFirstname());
            required.add(userDTO.getLastname());

            for (String s : required) {
                if (s == null || s.equals("")) {
                    return Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity(Messages.CREATE_USER_WITH_MISSING_FIELDS)
                            .build();
                }
            }

            if (em.find(User.class, userDTO.getUsername()) != null) {
                _logger.debug("User already exists");
                return Response
                        .status(Response.Status.CONFLICT)
                        .entity(Messages.CREATE_USER_WITH_NON_UNIQUE_NAME)
                        .build();
            }

            User user = UserMapper.toDomain(userDTO);

            String authToken = UUID.randomUUID().toString();
            user.setAuthToken(authToken);
            user.setAuthTokenTimeStamp(LocalDate.now());
            _logger.debug("Generated Auth token: " + authToken);

            em.persist(user);

            em.getTransaction().commit();

            _logger.debug("Created user with id: " + user.getUsername());
            return Response
                    .created(URI.create("/users/" + user.getUsername()))
                    .cookie(new NewCookie("AuthToken", user.getAuthToken()))
                    .build();
        } catch (Exception e) {
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }

    @PUT
    @Path("/auth")
    @Consumes(MediaType.APPLICATION_XML)
    public Response authenticateUser(UserDTO userDTO) {
        EntityManager em = _persistenceManager.createEntityManager();

        try {
            _logger.debug("Authenticating user: " + userDTO.getUsername());

            em.getTransaction().begin();

            User user = em.find(User.class, userDTO.getUsername());

            // Missing username or password
            List<String> required = new ArrayList<>();
            required.add(userDTO.getUsername());
            required.add(userDTO.getPassword());

            for (String s : required) {
                if (s == null || s.equals("")) {
                    return Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity(Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS)
                            .build();
                }
            }

            // User not found
            if (user == null) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(Messages.AUTHENTICATE_NON_EXISTENT_USER)
                        .build();
            }

            // Incorrect password
            if (!user.getPassword().equals(userDTO.getPassword())) {
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity(Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD)
                        .build();
            }

            if (user.getAuthToken() == null) {
                String authToken = UUID.randomUUID().toString();
                user.setAuthToken(authToken);
                user.setAuthTokenTimeStamp(LocalDate.now());
                _logger.debug("Generated Auth token: " + authToken);
                em.merge(user);
            }

            em.getTransaction().commit();

            return Response
                    .accepted(UserMapper.toDTO(user))
                    .cookie(new NewCookie("AuthToken", user.getAuthToken()))
                    .build();
        } catch (Exception e) {
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }

    @POST
    @Path("/credit_card")
    public Response addCreditCard(CreditCardDTO creditCardDTO,
                                  @CookieParam("AuthToken") Cookie authToken) {
        EntityManager em = _persistenceManager.createEntityManager();

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

            CreditCard creditCard = CreditCardMapper.toDomain(creditCardDTO);

            _logger.debug("Registering credit card with #: " + creditCard.get_number() +
                    ", name: " + creditCard.get_name() + ", expiry: " + creditCard.get_expiryDate() +
                    ", type: " + creditCard.get_type());

            user.setCreditCard(creditCard);

            em.persist(creditCard);
            em.merge(user);

            em.getTransaction().commit();

            return Response.accepted().build();
        } catch (Exception e) {
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }
}
