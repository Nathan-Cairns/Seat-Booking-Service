package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.NewsItemDTO;
import nz.ac.auckland.concert.common.types.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;

public class NewsItemCallback implements InvocationCallback<NewsItemDTO> {

    private WebTarget target;
    private Subscription subscription;
    private Cookie cookie;

    private Logger logger = LoggerFactory
            .getLogger(NewsItemCallback.class);

    public NewsItemCallback(WebTarget target, Subscription subscription, Cookie cookie) {
        this.target = target;
        this.subscription = subscription;
        this.cookie = cookie;
    }

    @Override
    public void completed(NewsItemDTO newsItemDTO) {
        this.logger.debug("Received news item: " + newsItemDTO.getTitle());
        this.logger.debug("With body: " + newsItemDTO.getBody());
        this.subscription.setNewsItemDTO(newsItemDTO);
        this.target.request()
                .accept(MediaType.APPLICATION_XML)
                .cookie("AuthToken", cookie.getValue())
                .async()
                .get(this);
    }

    @Override
    public void failed(Throwable throwable) {
        throw new ServiceException(throwable.getMessage());
    }
}
