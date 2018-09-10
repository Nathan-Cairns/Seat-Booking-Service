package nz.ac.auckland.concert.service.services.resources;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;

public interface SubscriptionResource<T> {
    Response unsubscribe(Cookie authToken);

    Response subscribe(AsyncResponse response,  Cookie authToken);

    void process(T t);

}
