package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.service.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

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
        _logger.debug("Retrieving user with id " + id);

        EntityManager em = _persistenceManager.createEntityManager();

        em.getTransaction().begin();

        User user = em.find(User.class, id);

        if (user == null) {
            _logger.debug("Could not find user with id: " + id);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        _logger.debug("Retrieved user with id: " + id);
        return Response.ok(user).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response postUser(User user){
        _logger.debug("Creating new user with id: " + user.getId());

        EntityManager em = _persistenceManager.createEntityManager();

        em.getTransaction().begin();

        em.persist(user);

        em.getTransaction().commit();

        _logger.debug("Created user with id: " + user.getId());
        return Response.created(URI.create("/users/" + user.getId())).build();
    }
}
