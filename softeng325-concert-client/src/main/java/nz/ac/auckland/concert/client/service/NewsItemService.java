package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.NewsItemDTO;

public interface NewsItemService {
    void createNewsItem(NewsItemDTO newsItemDTO) throws ServiceException;

    void cancelNewsItemSub() throws ServiceException;

    void newsItemSub() throws ServiceException;
}
