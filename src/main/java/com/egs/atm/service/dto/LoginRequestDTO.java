package com.egs.atm.service.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * DTO class relative to {@link com.egs.atm.domain.Account}
 * */
@Data
public class LoginRequestDTO implements Serializable {

    private String accountNumber;
    private String pin;


    /**
     * set account number using builder pattern
     * */
    public LoginRequestDTO name(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    /**
     * set account pin using builder pattern
     * */
    public LoginRequestDTO pin(String pin) {
        this.pin = pin;
        return this;
    }



}
