package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.service.domain.*;
import nz.ac.auckland.concert.service.services.resources.*;

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

        _classes.add(ConcertResource.class);
        _classes.add(PerformerResource.class);
        _classes.add(UserResource.class);
        _classes.add(BookingResource.class);

        _singletons.add(new NewsItemResource());

        _persistenceManager = PersistenceManager.instance();

        this.clearDB();
    }

    @Override
    public Set<Class<?>> getClasses() {
        return _classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return _singletons;
    }

    private void clearDB() {
        EntityManager em = _persistenceManager.createEntityManager();

        try {
            em.getTransaction().begin();

            List<User> users = em
                    .createQuery("SELECT u FROM User u", User.class)
                    .getResultList();

            List<Seat> seats = em
                    .createQuery("SELECT s FROM Seat s", Seat.class)
                    .getResultList();

            List<CreditCard> creditCards = em
                    .createQuery("SELECT c FROM CreditCard c", CreditCard.class)
                    .getResultList();

            List<Reservation> reservations = em
                    .createQuery("SELECT r FROM Reservation r", Reservation.class)
                    .getResultList();

            List<NewsItem> newsItems = em
                    .createQuery("SELECT n FROM NewsItem n", NewsItem.class)
                    .getResultList();

            for (User u : users) {
                em.remove(u);
            }

            for(Seat s: seats) {
                em.remove(s);
            }

            for (CreditCard c : creditCards) {
                em.remove(c);
            }

            for (Reservation r : reservations) {
                em.remove(r);
            }

            for (NewsItem n : newsItems) {
                em.remove(n);
            }

            em.flush();
            em.clear();

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
