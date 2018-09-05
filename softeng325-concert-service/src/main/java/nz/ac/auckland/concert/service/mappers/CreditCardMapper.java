package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.service.domain.CreditCard;

public class CreditCardMapper {

    public static CreditCard toDomain(CreditCardDTO creditCardDTO) {
        return new CreditCard(
                creditCardDTO.getType(),
                creditCardDTO.getName(),
                creditCardDTO.getNumber(),
                creditCardDTO.getExpiryDate()
        );
    }
}
