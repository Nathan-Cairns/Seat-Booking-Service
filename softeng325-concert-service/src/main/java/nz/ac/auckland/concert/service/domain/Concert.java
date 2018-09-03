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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CID", nullable = false, unique = true)
    private long id;

    @Column(name="TITLE", nullable = false)
    private String title;

    @ElementCollection
    @CollectionTable(name = "CONCERT_DATES", joinColumns = @JoinColumn(name="CID"))
    @Column(name = "DATE_TIME", nullable = false, unique = true)
    @Convert(converter=LocalDateTimeConverter.class)
    private Set<LocalDateTime> dates;

    @ManyToMany
    @JoinTable(name = "CONCERT_PERFORMER", joinColumns = @JoinColumn(name="CID"),
            inverseJoinColumns = @JoinColumn(name="PID"))
    @Column(name = "PERFORMER", nullable = false, unique = true)
    private Set<Performer> performers;

    @ElementCollection
    @JoinTable(name="CONCERT_TARIFS", joinColumns = @JoinColumn(name="CID"))
    @MapKeyColumn(name = "PRICE_BAND", nullable = false)
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
