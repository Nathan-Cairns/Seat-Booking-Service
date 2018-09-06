package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.NewsItemDTO;
import nz.ac.auckland.concert.service.domain.NewsItem;

public class NewsItemMapper {
    public static NewsItemDTO toDTO(NewsItem newsItem) {
        return new NewsItemDTO(
                newsItem.getId(),
                newsItem.getTitle(),
                newsItem.getBody(),
                newsItem.getPubDate()
        );
    }
}
