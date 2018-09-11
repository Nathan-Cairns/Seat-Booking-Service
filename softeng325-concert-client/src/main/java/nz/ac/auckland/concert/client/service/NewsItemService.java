package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.NewsItemDTO;

/**
 * An interface simplifying the development of clients which can subscribe to recieve news items.
 */
public interface NewsItemService {

    /**
     * A method for creating a new news item.
     *
     * @param newsItemDTO The news item to create on the server
     *
     * @throws ServiceException in response to any of the following conditions.
     * The exception's message is defined in
     * class nz.ac.auckland.concert.common.Messages.
     *
     * Condition: there is a communication error.
     * Messages.SERVICE_COMMUNICATION_ERROR
     */
    void createNewsItem(NewsItemDTO newsItemDTO) throws ServiceException;

    /**
     * Cancels currently authenticated users subscription
     *
     * @throws ServiceException in response to any of the following conditions.
     * The exception's message is defined in
     * class nz.ac.auckland.concert.common.Messages.
     *
     * Condition: the request is made by an unauthenticated user.
     * Messages.UNAUTHENTICATED_REQUEST
     *
     * Condition: the request includes an authentication token but it's not
     * recognised by the remote service.
     * Messages.BAD_AUTHENTICATON_TOKEN
     *
     * Condition: there is a communication error.
     * Messages.SERVICE_COMMUNICATION_ERROR
     */
    void cancelNewsItemSub() throws ServiceException;

    /**
     * Subscribes currently authenticated user to recieve news items
     *
     * @throws ServiceException in response to any of the following conditions.
     * The exception's message is defined in
     * class nz.ac.auckland.concert.common.Messages.
     *
     * Condition: the request is made by an unauthenticated user.
     * Messages.UNAUTHENTICATED_REQUEST
     *
     * Condition: the request includes an authentication token but it's not
     * recognised by the remote service.
     * Messages.BAD_AUTHENTICATON_TOKEN
     *
     * Condition: there is a communication error.
     * Messages.SERVICE_COMMUNICATION_ERROR
     */
    void newsItemSub() throws ServiceException;


    /**
     * Returns the current news item the client is reading.
     *
     * @throws ServiceException in response to any of the following conditions.
     * The exception's message is defined in
     * class nz.ac.auckland.concert.common.Messages.
     *
     * Condition: there is a communication error.
     * Messages.SERVICE_COMMUNICATION_ERROR
     */
    NewsItemDTO getCurrentNewsItem() throws ServiceException;
}
