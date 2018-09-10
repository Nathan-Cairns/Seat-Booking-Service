package nz.ac.auckland.concert.service.services.resources;

import javax.ws.rs.CookieParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;

public interface SubscriptionResource<T> {
    Response unsubscribe(@CookieParam("AuthToke") Cookie authToken);

    Response subscribe(@Suspended AsyncResponse response, @CookieParam("AuthToken") Cookie authToken);

    void process(T t);

}
