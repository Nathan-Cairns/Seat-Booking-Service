package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.Genre;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="PERFORMERS")
public class Performer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PID", nullable = false)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "GENRE", nullable = false)
    private Genre genre;

    @Column(name="IMAGE_NAME", nullable = false)
    private String imageName;

    @Column(name="NAME", nullable = false)
    private String name;

    @ManyToMany
    @JoinTable(name = "CONCERT_PERFORMERS", joinColumns = @JoinColumn(name="PID"),
            inverseJoinColumns = @JoinColumn(name="CID"))
    private Set<Concert> concerts;

    public Performer() {}

    public long getId() {
        return id;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Concert> getConcerts() {
        return concerts;
    }

    public Set<Long> getConcertIds() {
        Set<Long> ids = new HashSet<>();

        for (Concert concert : concerts) {
            ids.add(concert.getId());
        }

        return ids;
    }
}
