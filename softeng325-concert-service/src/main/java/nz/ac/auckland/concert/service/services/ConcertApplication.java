package nz.ac.auckland.concert.service.services;

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

        _classes.add(ConcertResource.class);
        _classes.add(PerformerResource.class);
        _classes.add(UserResource.class);

        _singletons.add(PersistenceManager.instance());
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
