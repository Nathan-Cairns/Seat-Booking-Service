package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.PerformerDTO;

public interface PerformerSubscriptionService {

    void createPerformer(PerformerDTO performerDTO) throws ServiceException;

    void cancelPerformerSub() throws ServiceException;

    void performerSub() throws  ServiceException;
}
