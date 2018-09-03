package nz.ac.auckland.concert.service.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
public class Concert {

    @Id
    @GeneratedValue
    private long id;

    private String title;
    private Set<LocalDateTime> dates;
    private Set<Performer> performers;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<LocalDateTime> getDates() {
        return dates;
    }

    public Set<Performer> getPerformers() {
        return performers;
    }
}
