package nz.ac.auckland.concert.service.services.resources;

import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.service.domain.User;
import nz.ac.auckland.concert.service.mappers.UserMapper;
import nz.ac.auckland.concert.service.services.PersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

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

            String u = userDTO.getUsername();
            String p = userDTO.getPassword();
            String fn = userDTO.getFirstname();
            String ln = userDTO.getLastname();

            if (u == null || u.equals("") || p == null || p.equals("")
                    || fn == null || fn.equals("") || ln == null || ln.equals("")) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Messages.CREATE_USER_WITH_MISSING_FIELDS)
                        .build();
            }

            if (em.find(User.class, userDTO.getUsername()) != null) {
                _logger.debug("User already exists");
                return Response.status(Response.Status.CONFLICT)
                        .entity(Messages.CREATE_USER_WITH_NON_UNIQUE_NAME)
                        .build();
            }

            em.getTransaction().commit();

            User user = UserMapper.toDomain(userDTO);

            em.getTransaction().begin();

            em.persist(user);

            em.getTransaction().commit();

            _logger.debug("Created user with id: " + user.getUsername());
            return Response.created(URI.create("/users/" + user.getUsername())).build();
        } catch (Exception e) {
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }
}
