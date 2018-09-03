package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.PriceBand;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Entity
public class Concert {

    @Id
    @GeneratedValue
    private long id;

    private String title;
    private Set<LocalDateTime> dates;
    private Set<Performer> performers;

    private Map<PriceBand, BigDecimal> tariff;
    private Set<Long> performerIds;

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

    public void setDates(Set<LocalDateTime> dates) {
        this.dates = dates;
    }

    public Set<Performer> getPerformers() {
        return performers;
    }

    public void setPerformers(Set<Performer> performers) {
        this.performers = performers;
    }

    public Map<PriceBand, BigDecimal> getTariff() {
        return tariff;
    }

    public void setTariff(Map<PriceBand, BigDecimal> tariff) {
        this.tariff = tariff;
    }

    public Set<Long> getPerformerIds() {
        return performerIds;
    }

    public void setPerformerIds(Set<Long> performerIds) {
        this.performerIds = performerIds;
    }
}
