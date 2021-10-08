package com.egs.atm.service;

import com.egs.atm.ATMApp;
import com.egs.atm.domain.Account;
import com.egs.atm.domain.AccountTransaction;
import com.egs.atm.domain.enumration.TransactionType;
import com.egs.atm.repository.AccountRepository;
import com.egs.atm.repository.AccountTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = ATMApp.class)
@ActiveProfiles("dev")
public class AccountTransactionServiceTest {
    private static final String DEFAULT_CARD_NUMBER = "3333-3333-3333-3333";
    private static final String DEFAULT_PIN = "1234";


    @Autowired
    private AccountTransactionService accountTransactionService;
    @Autowired
    private AccountTransactionRepository accountTransactionRepository;
    @Autowired
    private AccountRepository accountRepository;


    private Account account;


    public Account createEntity() {
        accountTransactionRepository.deleteAll();
        accountRepository.deleteAll();
        Account account = new Account()
                .accountNumber(DEFAULT_CARD_NUMBER)
                .pin(new BCryptPasswordEncoder().encode(DEFAULT_PIN));
        account = accountRepository.save(account);
        return account;
    }


    @BeforeEach
    public void initTest() {
        accountTransactionRepository.deleteAll();
        accountRepository.deleteAll();
        account = createEntity();
    }

    @Test
    @Transactional
    public void calculateHashTest() throws Exception {

        AccountTransaction accountTransaction = generateAccountTransaction(accountRepository.findById(account.getId()).get(), BigDecimal.TEN, TransactionType.DEPOSIT);
        AccountTransaction accountTransaction1 = generateAccountTransaction(accountRepository.findById(account.getId()).get(), BigDecimal.TEN, TransactionType.DEPOSIT);

        assertThat(accountTransactionService.calculateHash(accountTransaction)).isEqualTo(accountTransactionService.calculateHash(accountTransaction));
        assertThat(accountTransactionService.calculateHash(accountTransaction)).isNotEqualTo(accountTransactionService.calculateHash(accountTransaction1));
    }

    @Test
    public void calculateBalanceTest() throws Exception {
        generateAccountTransaction(accountRepository.findById(account.getId()).get(), BigDecimal.TEN, TransactionType.DEPOSIT);
        assertThat(accountTransactionService.calculateBalance(accountRepository.findById(account.getId()).get()).compareTo(BigDecimal.valueOf(10)) == 0);

        generateAccountTransaction(accountRepository.findById(account.getId()).get(), BigDecimal.TEN, TransactionType.DEPOSIT);
        assertThat(accountTransactionService.calculateBalance(accountRepository.findById(account.getId()).get()).compareTo(BigDecimal.valueOf(20)) == 0);

        generateAccountTransaction(accountRepository.findById(account.getId()).get(), BigDecimal.TEN, TransactionType.WITHDRAWAL);
        assertThat(accountTransactionService.calculateBalance(accountRepository.findById(account.getId()).get()).compareTo(BigDecimal.valueOf(10)) == 0);

        generateAccountTransaction(accountRepository.findById(account.getId()).get(), BigDecimal.TEN, TransactionType.WITHDRAWAL);
        assertThat(accountTransactionService.calculateBalance(accountRepository.findById(account.getId()).get()).compareTo(BigDecimal.valueOf(0)) == 0);

    }


    private AccountTransaction generateAccountTransaction(Account account, BigDecimal amount, TransactionType transactionType) throws Exception {
        BigDecimal balance = calBalance(account.getLastAccountTransaction(), amount, transactionType);
        AccountTransaction accountTransaction = new AccountTransaction()
                .account(account)
                .balance(balance)
                .amount(amount)
                .transactionType(transactionType);
        accountTransactionRepository.save(accountTransaction);
        accountTransaction.setHashData(accountTransactionService.calculateHash(accountTransaction));
        accountTransactionRepository.save(accountTransaction);
        account.setLastAccountTransaction(accountTransaction);
        accountRepository.save(account);
        return accountTransaction;
    }

    private BigDecimal calBalance(AccountTransaction lastAccountTransaction, BigDecimal amount, TransactionType transactionType) throws Exception {
        if (lastAccountTransaction == null) {
            if (transactionType.equals(TransactionType.DEPOSIT)) {
                return amount;
            } else throw new Exception("Bad Data");
        } else if (transactionType.equals(TransactionType.DEPOSIT)) {
            return lastAccountTransaction.getBalance().add(amount);
        } else {
            if (lastAccountTransaction.getBalance().compareTo(amount) < 0) {
                throw new Exception("Bad Data");
            } else return lastAccountTransaction.getBalance().subtract(amount);
        }
    }

}
