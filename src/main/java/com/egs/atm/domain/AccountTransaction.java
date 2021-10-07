package com.egs.atm.domain;

import com.egs.atm.domain.enumration.TransactionType;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Data
public class AccountTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private BigDecimal amount;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private TransactionType transactionType;

    @ManyToOne
    private Account account;


    public AccountTransaction amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }
    public AccountTransaction transactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
        return this;
    }
    public AccountTransaction account(Account account) {
        this.account = account;
        return this;
    }




    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountTransaction)) {
            return false;
        }
        return id != null && id.equals(((AccountTransaction) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
