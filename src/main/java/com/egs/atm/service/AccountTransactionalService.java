package com.egs.atm.service;

import com.egs.atm.domain.Account;
import com.egs.atm.domain.AccountTransaction;
import com.egs.atm.domain.enumration.TransactionType;
import com.egs.atm.repository.AccountRepository;
import com.egs.atm.repository.AccountTransactionalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
public class AccountTransactionalService {

    private final Logger log = LoggerFactory.getLogger(AccountTransactionalService.class);

    private final AccountTransactionalRepository accountTransactionalRepository;
    private final AccountRepository accountRepository;


    public AccountTransactionalService(AccountTransactionalRepository accountTransactionalRepository, AccountRepository accountRepository) {
        this.accountTransactionalRepository = accountTransactionalRepository;
        this.accountRepository = accountRepository;
    }

    public BigDecimal depositAccount(BigDecimal amount) {
        AccountTransaction accountTransaction = new AccountTransaction()
                .account(findAccount())
                .amount(amount)
                .transactionType(TransactionType.DEPOSIT);
        accountTransactionalRepository.save(accountTransaction);

        return checkBalance();
    }

    public BigDecimal withdrawalAccount(BigDecimal amount) throws Exception {
        if (checkBalance().compareTo(amount) < 0) {
            throw new Exception("");
        }
        AccountTransaction accountTransaction = new AccountTransaction()
                .account(findAccount())
                .amount(amount)
                .transactionType(TransactionType.WITHDRAWAL);
        accountTransactionalRepository.save(accountTransaction);

        return checkBalance();
    }

    public BigDecimal checkBalance() {
        return accountTransactionalRepository.checkBalance(findAccount());
    }


    private Account findAccount() {
        String username;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(username);
        if (accountOptional.isPresent()) {
            return accountOptional.get();
        } else throw new UsernameNotFoundException("User not found with username: " + username);
    }
}

