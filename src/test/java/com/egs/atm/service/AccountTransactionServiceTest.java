package com.egs.atm.service;

import com.egs.atm.ATMApp;
import com.egs.atm.domain.Account;
import com.egs.atm.domain.AccountTransaction;
import com.egs.atm.domain.enumration.TransactionType;
import com.egs.atm.repository.AccountRepository;
import com.egs.atm.repository.AccountTransactionalRepository;
import com.egs.atm.service.dto.AccountDTO;
import com.egs.atm.service.mapper.AccountMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = ATMApp.class)
public class AccountTransactionServiceTest {


    @Autowired
    private AccountTransactionalService accountTransactionalService;
    @Autowired
    private AccountTransactionalRepository accountTransactionalRepository;
    @Autowired
    private AccountRepository accountRepository;


    private Account account;


    public Account createEntity() {
        accountTransactionalRepository.deleteAll();
        accountRepository.deleteAll();
        Account account = new Account()
                .accountNumber("1111-1111-1111-1111")
                .pin(new BCryptPasswordEncoder().encode("1234"));
        account = accountRepository.save(account);
        return account;
    }


    @BeforeEach
    public void initTest() {
        accountTransactionalRepository.deleteAll();
        accountRepository.deleteAll();
        account = createEntity();
    }

    @Test
    @Transactional
    public void calculateHashTest() throws Exception {

        AccountTransaction accountTransaction = generateAccountTransaction(accountRepository.findById(account.getId()).get(), BigDecimal.TEN, TransactionType.DEPOSIT);
        AccountTransaction accountTransaction1 = generateAccountTransaction(accountRepository.findById(account.getId()).get(), BigDecimal.TEN, TransactionType.DEPOSIT);

        assertThat(accountTransactionalService.calculateHash(accountTransaction)).isEqualTo(accountTransactionalService.calculateHash(accountTransaction));
        assertThat(accountTransactionalService.calculateHash(accountTransaction)).isNotEqualTo(accountTransactionalService.calculateHash(accountTransaction1));
    }

    @Test
    public void calculateBalanceTest() throws Exception {
        generateAccountTransaction(accountRepository.findById(account.getId()).get(), BigDecimal.TEN, TransactionType.DEPOSIT);
        assertThat(accountTransactionalService.calculateBalance(accountRepository.findById(account.getId()).get()).compareTo(BigDecimal.valueOf(10)) == 0);

        generateAccountTransaction(accountRepository.findById(account.getId()).get(), BigDecimal.TEN, TransactionType.DEPOSIT);
        assertThat(accountTransactionalService.calculateBalance(accountRepository.findById(account.getId()).get()).compareTo(BigDecimal.valueOf(20)) == 0);

        generateAccountTransaction(accountRepository.findById(account.getId()).get(), BigDecimal.TEN, TransactionType.WITHDRAWAL);
        assertThat(accountTransactionalService.calculateBalance(accountRepository.findById(account.getId()).get()).compareTo(BigDecimal.valueOf(10)) == 0);

        generateAccountTransaction(accountRepository.findById(account.getId()).get(), BigDecimal.TEN, TransactionType.WITHDRAWAL);
        assertThat(accountTransactionalService.calculateBalance(accountRepository.findById(account.getId()).get()).compareTo(BigDecimal.valueOf(0)) == 0);

    }


    private AccountTransaction generateAccountTransaction(Account account, BigDecimal amount, TransactionType transactionType) throws Exception {
        BigDecimal balance = calBalance(account.getLastAccountTransaction(), amount, transactionType);
        AccountTransaction accountTransaction = new AccountTransaction()
                .account(account)
                .balance(balance)
                .amount(amount)
                .transactionType(transactionType);
        accountTransactionalRepository.save(accountTransaction);
        accountTransaction.setHashData(accountTransactionalService.calculateHash(accountTransaction));
        accountTransactionalRepository.save(accountTransaction);
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
