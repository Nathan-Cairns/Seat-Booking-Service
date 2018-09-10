package nz.ac.auckland.concert.common.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;

import javax.xml.bind.annotation.*;
import java.time.LocalDateTime;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "news_item")
public class NewsItemDTO {

    @XmlAttribute(name="id")
    private long id;

    @XmlElement(name = "title")
    private String title;

    @XmlElement(name="body")
    private String body;

    @XmlElement(name = "date")
    private LocalDateTime pubDate;

    public NewsItemDTO() {}

    public NewsItemDTO(long id, String title, String body, LocalDateTime pubDate) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.pubDate = pubDate;
    }

    public NewsItemDTO(String title, String body, LocalDateTime pubDate) {
        this.title = title;
        this.body = body;
        this.pubDate = pubDate;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDateTime getPubDate() {
        return pubDate;
    }

    public void setPubDate(LocalDateTime pubDate) {
        this.pubDate = pubDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NewsItemDTO)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        NewsItemDTO rhs = (NewsItemDTO) obj;
        return new EqualsBuilder()
                .append(this.title, rhs.title)
                .append(this.body, this.body)
                .append(this.getPubDate(), this.getPubDate())
                .isEquals();
    }
}
