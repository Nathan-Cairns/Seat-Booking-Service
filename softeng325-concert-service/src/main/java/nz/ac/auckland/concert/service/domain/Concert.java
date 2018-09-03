package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.service.domain.jpa.LocalDateTimeConverter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name="CONCERTS")
public class Concert {

    @Column(name = "ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="TITLE")
    private String title;

    @CollectionTable(name = "CONCERT_DATES")
    @ElementCollection
    @Convert(converter=LocalDateTimeConverter.class)
    private Set<LocalDateTime> dates;

    @ManyToMany
    @JoinTable(name = "CONCERT_PERFORMER", joinColumns = @JoinColumn(name="concert_id"),
            inverseJoinColumns = @JoinColumn(name="performer_id"))
    private Set<Performer> performers;

    @CollectionTable(name = "CONCERT_TARIFS")
    @ElementCollection
    @MapKeyColumn(name="PRICE_BAND")
    @MapKeyEnumerated(EnumType.STRING)
    private Map<PriceBand, BigDecimal> tariff;

    public Concert(){}

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
        Set<Long> performerIds = new HashSet<>();
        for (Performer performer : performers) {
            performerIds.add(performer.getId());
        }
        return performerIds;
    }
}
