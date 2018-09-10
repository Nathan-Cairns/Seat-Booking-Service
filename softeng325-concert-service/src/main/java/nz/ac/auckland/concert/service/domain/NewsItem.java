package nz.ac.auckland.concert.service.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "NEWS_ITEM")
public class NewsItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NID")
    private long id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "BODY", length = 2000)
    private String body;

    @Column(name = "PUB_DATE")
    private LocalDateTime pubDate;

    public NewsItem() {

    }

    public NewsItem(String title, String body, LocalDateTime pubDate) {
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
}
