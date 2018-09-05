package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "CREDIT_CARDS")
public class CreditCard {

    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private CreditCardDTO.Type _type;

    @Column(name = "NAME", nullable = false)
    private String _name;

    @Id
    @Column(name = "NUMBER", nullable = false, unique = true)
    private String _number;

    @Column(name = "EXPIRY", nullable = false)
    private LocalDate _expiryDate;

    public CreditCard(){}

    public CreditCard(CreditCardDTO.Type _type, String _name, String _number, LocalDate _expiryDate) {
        this._type = _type;
        this._name = _name;
        this._number = _number;
        this._expiryDate = _expiryDate;
    }

    public CreditCardDTO.Type get_type() {
        return _type;
    }

    public void set_type(CreditCardDTO.Type _type) {
        this._type = _type;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_number() {
        return _number;
    }

    public void set_number(String _number) {
        this._number = _number;
    }

    public LocalDate get_expiryDate() {
        return _expiryDate;
    }

    public void set_expiryDate(LocalDate _expiryDate) {
        this._expiryDate = _expiryDate;
    }
}
