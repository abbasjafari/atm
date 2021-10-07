package com.egs.atm.repository;

import com.egs.atm.domain.Account;
import com.egs.atm.domain.AccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface AccountTransactionalRepository extends JpaRepository<AccountTransaction, Long>, JpaSpecificationExecutor<AccountTransaction> {

    @Query(
            "select " +
                    "sum(case when at.transactionType=com.egs.atm.domain.enumration.TransactionType.DEPOSIT then at.amount else (-1*at.amount) end)" +
                    " from AccountTransaction at" +
                    " where at.account=:account"
    )
    BigDecimal checkBalance(@Param("account") Account account);
}
