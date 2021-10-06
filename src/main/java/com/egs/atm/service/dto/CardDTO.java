package com.egs.atm.service.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class CardDTO implements Serializable {

    private Long id;
    private String cardNumber;
    private String pin;


    public CardDTO name(String cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }

    public CardDTO pin(String pin) {
        this.pin = pin;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CardDTO cardDTO = (CardDTO) o;
        if (cardDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), cardDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
