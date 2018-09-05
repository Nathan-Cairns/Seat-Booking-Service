package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.service.services.resources.ConcertResource;
import nz.ac.auckland.concert.service.services.resources.PerformerResource;
import nz.ac.auckland.concert.service.services.resources.BookingResource;
import nz.ac.auckland.concert.service.services.resources.UserResource;
import nz.ac.auckland.concert.service.domain.User;

import javax.persistence.EntityManager;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationPath("/services")
public class ConcertApplication extends Application {
    private Set<Class<?>> _classes;
    private Set<Object> _singletons;

    private PersistenceManager _persistenceManager;

    public ConcertApplication() {
        super();
        _classes = new HashSet<>();
        _singletons = new HashSet<>();

        _singletons.add(new ConcertResource());
        _singletons.add(new PerformerResource());
        _singletons.add(new UserResource());
        _singletons.add(new BookingResource());

        _persistenceManager = PersistenceManager.instance();

        this.clearUsers();
    }

    @Override
    public Set<Class<?>> getClasses() {
        return _classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return _singletons;
    }

    private void clearUsers () {
        EntityManager em = _persistenceManager.createEntityManager();

        try {
            em.getTransaction().begin();

            List<User> users = em.createQuery("SELECT u FROM User u", User.class).getResultList();

            for (User u : users) {
                em.remove(u);
            }

            em.flush();
            em.clear();

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
