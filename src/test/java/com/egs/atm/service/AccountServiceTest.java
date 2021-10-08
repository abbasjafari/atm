/*
package com.egs.atm.service;

import com.egs.atm.ATMApp;
import com.egs.atm.domain.Account;
import com.egs.atm.repository.AccountRepository;
import com.egs.atm.service.dto.AccountDTO;
import com.egs.atm.service.mapper.AccountMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = ATMApp.class)
public class AccountServiceTest {

    private static final String DEFAULT_CARD_NUMBER = "AAAAAAAAAA";

    private static final String DEFAULT_PIN = "AAAAAAAAAA";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private AccountService accountService;


    @Autowired
    private EntityManager em;


    private Account account;


    public static Account createEntity(EntityManager em) {
        Account account = new Account()
                .accountNumber(DEFAULT_CARD_NUMBER)
                .pin(DEFAULT_PIN);
        return account;
    }


    @BeforeEach
    public void initTest() {
        account = createEntity(em);
    }

    @Test
    @Transactional
    public void createAccount() {
        int databaseSizeBeforeCreate = accountRepository.findAll().size();

        // Create the Account
        AccountDTO accountDTO = accountMapper.toDto(account);
        accountService.save(accountDTO);

        // Validate the Account in the database
        List<Account> accountList = accountRepository.findAll();
        assertThat(accountList).hasSize(databaseSizeBeforeCreate + 1);
        Account testAccount = accountList.get(accountList.size() - 1);
        assertThat(testAccount.getAccountNumber()).isEqualTo(DEFAULT_CARD_NUMBER);
        assertThat(testAccount.getPin()).isEqualTo(DEFAULT_PIN);
    }

}
*/
