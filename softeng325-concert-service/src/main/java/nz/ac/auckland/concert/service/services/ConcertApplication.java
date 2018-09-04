package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.service.services.resources.ConcertResource;
import nz.ac.auckland.concert.service.services.resources.PerformerResource;
import nz.ac.auckland.concert.service.services.resources.UserResource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/services")
public class ConcertApplication extends Application {
    private Set<Class<?>> _classes;
    private Set<Object> _singletons;

    public ConcertApplication() {
        super();
        _classes = new HashSet<>();
        _singletons = new HashSet<>();

        _singletons.add(new ConcertResource());
        _singletons.add(new PerformerResource());
        _singletons.add(new UserResource());

        PersistenceManager pm = PersistenceManager.instance();
    }

    @Override
    public Set<Class<?>> getClasses() {
        return _classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return _singletons;
    }
}
