
package com.egs.atm.web.rest;

import com.egs.atm.ATMApp;
import com.egs.atm.TestUtil;
import com.egs.atm.domain.Account;
import com.egs.atm.repository.AccountRepository;
import com.egs.atm.repository.AccountTransactionRepository;
import com.egs.atm.service.AccountService;
import com.egs.atm.service.AccountTransactionService;
import com.egs.atm.service.JwtAuthenticationService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Integration tests for the {@link AccountTransactionResource} REST controller.
 */

@SpringBootTest(classes = ATMApp.class)
public class AccountTransactionResourceITest {

    private static final String DEFAULT_CARD_NUMBER = "2222-2222-2222-2222";
    private static final String DEFAULT_PIN = "1234";


    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountTransactionRepository accountTransactionRepository;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountTransactionService accountTransactionService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private Validator validator;

    private MockMvc restAccountMockMvc;

    private MockMvc restAccountTransactionMockMvc;

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

        final AccountTransactionResource accountTransactionResource = new AccountTransactionResource(accountTransactionService, accountService);
        this.restAccountTransactionMockMvc = MockMvcBuilders.standaloneSetup(accountTransactionResource)
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

    public static Account createEntity() {
        Account account = new Account()
                .accountNumber(DEFAULT_CARD_NUMBER)
                .pin(DEFAULT_PIN);
        return account;
    }


    @BeforeEach
    public void initTest() {
        accountTransactionRepository.deleteAll();
        accountRepository.deleteAll();
        account = createEntity();
    }

    private void createAccount() throws Exception {
        // Create the Account
        AccountDTO accountDTO = accountMapper.toDto(account);
        restAccountMockMvc.perform(post("/api/accounts")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(accountDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @Transactional
    @WithMockUser(username = "2222-2222-2222-2222")
    public void depositAccount() throws Exception {
        createAccount();
        // start whit 0 balance
        BigDecimal balance = BigDecimal.ZERO;

        restAccountTransactionMockMvc.perform(post("/api/account-balance")
                .contentType(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().string(balance.toString()));

        //balance=10 => add 10 => status ok => balance=10
        balance = balance.add(BigDecimal.TEN);
        restAccountTransactionMockMvc.perform(post("/api/account-deposit")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .param("amount", BigDecimal.TEN.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(balance.toString()));
        restAccountTransactionMockMvc.perform(post("/api/account-balance")
                .contentType(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().string(balance.toString()));

        //balance=10 => subtract 10 => status ok => balance=0
        balance = balance.subtract(BigDecimal.TEN);
        restAccountTransactionMockMvc.perform(post("/api/account-withdrawal")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .param("amount", BigDecimal.TEN.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(balance.toString()));
        restAccountTransactionMockMvc.perform(post("/api/account-balance")
                .contentType(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().string(balance.toString()));

        //balance=0 => subtract 10 => bad request => balance=0
        restAccountTransactionMockMvc.perform(post("/api/account-withdrawal")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .param("amount", BigDecimal.TEN.toString()))
                .andExpect(status().isBadRequest());

        restAccountTransactionMockMvc.perform(post("/api/account-balance")
                .contentType(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().string(balance.toString()));

        //balance=0 => add 10 => status ok => balance=10
        balance = balance.add(BigDecimal.TEN);
        restAccountTransactionMockMvc.perform(post("/api/account-deposit")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .param("amount", BigDecimal.TEN.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(balance.toString()));
        restAccountTransactionMockMvc.perform(post("/api/account-balance")
                .contentType(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().string(balance.toString()));

        //balance=10 => subtract 10 => bad request => balance=10
        restAccountTransactionMockMvc.perform(post("/api/account-withdrawal")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .param("amount", new BigDecimal("100.00").toString()))
                .andExpect(status().isBadRequest());
        restAccountTransactionMockMvc.perform(post("/api/account-balance")
                .contentType(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().string(balance.toString()));

    }

}

