
package com.egs.atm.web.rest;

import com.egs.atm.ATMApp;
import com.egs.atm.TestUtil;
import com.egs.atm.domain.Account;
import com.egs.atm.repository.AccountRepository;
import com.egs.atm.repository.AccountTransactionRepository;
import com.egs.atm.service.AccountService;
import com.egs.atm.service.dto.AccountDTO;
import com.egs.atm.service.mapper.AccountMapper;
import com.egs.atm.web.rest.errors.ExceptionTranslator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Integration tests for the {@link AccountResource} REST controller.
 */

@SpringBootTest(classes = ATMApp.class)
public class AccountResourceITest {

    private static final String DEFAULT_CARD_NUMBER = "0000-0000-0000-0000";
    private static final String UPDATED_CARD_NUMBER = "1111-1111-1111-1111";

    private static final String DEFAULT_PIN = "1234";
    private static final String UPDATED_PIN = "4321";


    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountTransactionRepository accountTransactionRepository;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private AccountService accountService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restAccountMockMvc;

    private Account account;


    @BeforeEach
    public void setup() throws URISyntaxException {
        MockitoAnnotations.initMocks(this);
        final AccountResource accountResource = new AccountResource(accountService);
        this.restAccountMockMvc = MockMvcBuilders.standaloneSetup(accountResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setConversionService(TestUtil.createFormattingConversionService())
                .setMessageConverters(jacksonMessageConverter)
                .setValidator(validator).build();
    }


/**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */

    public static Account createEntity(EntityManager em) {
        Account account = new Account()
                .accountNumber(DEFAULT_CARD_NUMBER)
                .pin(DEFAULT_PIN);
        return account;
    }


/**
     * Create an updated entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */

    public static Account createUpdatedEntity(EntityManager em) {
        Account account = new Account()
                .accountNumber(UPDATED_CARD_NUMBER)
                .pin(UPDATED_PIN);
        return account;
    }

    @BeforeEach
    public void initTest() {
        accountTransactionRepository.deleteAll();
        accountRepository.deleteAll();
        account = createEntity(em);
    }

    @Test
    @Transactional
    public void createAccount() throws Exception {
        int databaseSizeBeforeCreate = accountRepository.findAll().size();

        // Create the Account
        AccountDTO accountDTO = accountMapper.toDto(account);
        restAccountMockMvc.perform(post("/api/accounts")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(accountDTO)))
                .andExpect(status().isCreated());

        // Validate the Account in the database
        List<Account> accountList = accountRepository.findAll();
        assertThat(accountList).hasSize(databaseSizeBeforeCreate + 1);
        Account testAccount = accountList.get(accountList.size() - 1);
        assertThat(testAccount.getAccountNumber()).isEqualTo(DEFAULT_CARD_NUMBER);
    }

    @Test
    @Transactional
    public void createAccountWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = accountRepository.findAll().size();

        // Create the Account with an existing ID
        account.setId(1L);
        AccountDTO accountDTO = accountMapper.toDto(account);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAccountMockMvc.perform(post("/api/accounts")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(accountDTO)))
                .andExpect(status().isBadRequest());

        // Validate the Account in the database
        List<Account> accountList = accountRepository.findAll();
        assertThat(accountList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllAccounts() throws Exception {
        // Initialize the database
        account=accountRepository.saveAndFlush(account);

        // Get all the accountList
        restAccountMockMvc.perform(get("/api/accounts?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].id").value(hasItem(account.getId().intValue())))
                .andExpect(jsonPath("$.[*].accountNumber").value(hasItem(DEFAULT_CARD_NUMBER)))
                .andExpect(jsonPath("$.[*].pin").value(hasItem(DEFAULT_PIN)));
    }

    @Test
    @Transactional
    public void getAccount() throws Exception {
        // Initialize the database
        account=accountRepository.saveAndFlush(account);

        // Get the account
        restAccountMockMvc.perform(get("/api/accounts/{id}", account.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.getId().intValue()))
                .andExpect(jsonPath("$.accountNumber").value(DEFAULT_CARD_NUMBER))
                .andExpect(jsonPath("$.pin").value(DEFAULT_PIN));
    }

    @Test
    @Transactional
    public void getNonExistingAccount() throws Exception {
        // Get the account
        restAccountMockMvc.perform(get("/api/accounts/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAccount() throws Exception {
        // Initialize the database
        account=accountRepository.saveAndFlush(account);

        int databaseSizeBeforeUpdate = accountRepository.findAll().size();

        // Update the account
        Account updatedAccount = accountRepository.findById(account.getId()).get();
        // Disconnect from session so that the updates on updatedAccount are not directly saved in db
        em.detach(updatedAccount);
        updatedAccount
                .accountNumber(UPDATED_CARD_NUMBER)
                .pin(UPDATED_PIN);
        AccountDTO accountDTO = accountMapper.toDto(updatedAccount);

        restAccountMockMvc.perform(put("/api/accounts")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(accountDTO)))
                .andExpect(status().isOk());

        // Validate the Account in the database
        List<Account> accountList = accountRepository.findAll();
        assertThat(accountList).hasSize(databaseSizeBeforeUpdate);
        Account testAccount = accountList.get(accountList.size() - 1);
        assertThat(testAccount.getAccountNumber()).isEqualTo(UPDATED_CARD_NUMBER);
    }

    @Test
    @Transactional
    public void updateNonExistingAccount() throws Exception {
        int databaseSizeBeforeUpdate = accountRepository.findAll().size();

        // Create the Account
        AccountDTO accountDTO = accountMapper.toDto(account);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAccountMockMvc.perform(put("/api/accounts")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(accountDTO)))
                .andExpect(status().isBadRequest());

        // Validate the Account in the database
        List<Account> accountList = accountRepository.findAll();
        assertThat(accountList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteAccount() throws Exception {
        // Initialize the database
        account=accountRepository.saveAndFlush(account);

        int databaseSizeBeforeDelete = accountRepository.findAll().size();

        // Delete the account
        restAccountMockMvc.perform(delete("/api/accounts/{id}", account.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Account> accountList = accountRepository.findAll();
        assertThat(accountList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Account.class);
        Account account1 = new Account();
        account1.setId(1L);
        Account account2 = new Account();
        account2.setId(account1.getId());
        assertThat(account1).isEqualTo(account2);
        account2.setId(2L);
        assertThat(account1).isNotEqualTo(account2);
        account1.setId(null);
        assertThat(account1).isNotEqualTo(account2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(AccountDTO.class);
        AccountDTO accountDTO1 = new AccountDTO();
        accountDTO1.setId(1L);
        AccountDTO accountDTO2 = new AccountDTO();
        assertThat(accountDTO1).isNotEqualTo(accountDTO2);
        accountDTO2.setId(accountDTO1.getId());
        assertThat(accountDTO1).isEqualTo(accountDTO2);
        accountDTO2.setId(2L);
        assertThat(accountDTO1).isNotEqualTo(accountDTO2);
        accountDTO1.setId(null);
        assertThat(accountDTO1).isNotEqualTo(accountDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        Assertions.assertThat(accountMapper.fromId(42L).getId()).isEqualTo(42);
        Assertions.assertThat(accountMapper.fromId(null)).isNull();
    }
}

