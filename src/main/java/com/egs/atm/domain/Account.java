package com.egs.atm.domain;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * ACCOUNT entity object
 * see also {@link com.egs.atm.service.dto.AccountDTO}
 * */
@Entity
@Data
public class Account implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @NotNull
    private String accountNumber;

    @Column
    @NotNull
    private String pin;

    @Column
    private Integer unsuccessfulCount=0;

    @ManyToOne
    private AccountTransaction lastAccountTransaction;

    @Version
    private Long version= 0L;

    public Account accountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public Account pin(String pin) {
        this.pin = pin;
        return this;
    }
    public Account unsuccessfulCount(Integer unsuccessfulCount) {
        this.unsuccessfulCount = unsuccessfulCount;
        return this;
    }
    public Account lastAccountTransaction(AccountTransaction lastAccountTransaction) {
        this.lastAccountTransaction = lastAccountTransaction;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Account)) {
            return false;
        }
        return id != null && id.equals(((Account) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
