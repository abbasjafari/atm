package com.egs.atm.service.dto;

import com.egs.atm.domain.AccountTransaction;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * DTO class relative to {@link com.egs.atm.domain.Account}
 * */
@Data
public class AccountDTO implements Serializable {

    private Long id;
    private String accountNumber;
    private String pin;
    private Integer unsuccessfulCount;
    private Long version;
    private ZonedDateTime expiryTime;


    /**
     * set account number using builder pattern
     * */
    public AccountDTO name(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    /**
     * set account pin using builder pattern
     * */
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
