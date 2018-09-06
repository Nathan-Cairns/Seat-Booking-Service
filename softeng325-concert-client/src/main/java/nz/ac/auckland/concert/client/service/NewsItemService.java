package nz.ac.auckland.concert.client.service;

public interface NewsItemService {
    void recieveNewsItem() throws ServiceException;

    void cancelNewsItemSub() throws ServiceException;

    void newsItemSub() throws ServiceException;
}
