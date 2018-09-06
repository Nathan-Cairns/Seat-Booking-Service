package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.NewsItemDTO;
import nz.ac.auckland.concert.common.message.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;

public class NewsItemCallback implements InvocationCallback<NewsItemDTO> {

    private Logger logger = LoggerFactory
            .getLogger(NewsItemCallback.class);

    private WebTarget target;

    public NewsItemCallback(WebTarget target) {
        this.target = target;
    }

    @Override
    public void completed(NewsItemDTO newsItemDTO) {
        this.logger.debug("Recieved news item: " + newsItemDTO.getTitle());
        this.logger.debug("With body: " + newsItemDTO.getBody());
        target.request().async().get(this);
    }

    @Override
    public void failed(Throwable throwable) {
        this.logger.debug(throwable.getMessage());
        throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
    }
}
