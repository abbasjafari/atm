package com.egs.atm.service.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class AccountDTO implements Serializable {

    private Long id;
    private String accountNumber;
    private String pin;


    public AccountDTO name(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public AccountDTO pin(String pin) {
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

        AccountDTO accountDTO = (AccountDTO) o;
        if (accountDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), accountDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
