package nz.ac.auckland.concert.service.domain;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="USER")
public class User {

    @Id
    @Column(name="USERNAME", nullable = false, unique = true)
    private String username;

    @Column(name="PASSWORD", nullable = false)
    private String password;

    @Column(name="FIRST_NAME", nullable = false)
    private String firstName;

    @Column(name="LAST_NAME", nullable = false)
    private String lastName;

    @ManyToOne
    @JoinColumn(name="CREDIT_CARD")
    private CreditCard creditCard;

    @Column(name="TOKEN")
    private String authToken;

    @Column(name="TOKEN_TIME_STAMP")
    private LocalDate authTokenTimeStamp;

    @ManyToOne
    @JoinColumn(name = "LAST_READ")
    private NewsItem lastRead;

    public User(){}

    public User(String username, String password, String firstName, String lastName) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public NewsItem getLastRead() {
        return lastRead;
    }

    public void setLastRead(NewsItem lastRead) {
        this.lastRead = lastRead;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public LocalDate getAuthTokenTimeStamp() {
        return authTokenTimeStamp;
    }

    public void setAuthTokenTimeStamp(LocalDate authTokenTimeStamp) {
        this.authTokenTimeStamp = authTokenTimeStamp;
    }
}
