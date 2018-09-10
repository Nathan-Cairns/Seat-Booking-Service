package nz.ac.auckland.concert.common.types;

import nz.ac.auckland.concert.common.dto.NewsItemDTO;

public class Subscription {

    private NewsItemDTO newsItemDTO;

    public NewsItemDTO getNewsItemDTO() {
        return this.newsItemDTO;
    }

    public void setNewsItemDTO(NewsItemDTO newsItemDTO) {
        this.newsItemDTO = newsItemDTO;
    }
}
