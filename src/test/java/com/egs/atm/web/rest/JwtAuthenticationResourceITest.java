
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
import com.egs.atm.service.dto.LoginRequestDTO;
import com.egs.atm.service.mapper.AccountMapper;
import com.egs.atm.web.rest.errors.ExceptionTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Integration tests for the {@link JwtAuthenticationResource} REST controller.
 */

@SpringBootTest(classes = ATMApp.class)
public class JwtAuthenticationResourceITest {

    private static final String DEFAULT_CARD_NUMBER_1 = "4444-4444-4444-4444";
    private static final String DEFAULT_PIN_1 = "1234";

    private static final String DEFAULT_CARD_NUMBER_2 = "5555-5555-5555-5555";
    private static final String DEFAULT_PIN_2 = "4321";


    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountTransactionRepository accountTransactionRepository;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private AccountService accountService;

    @Autowired
    private JwtAuthenticationService jwtAuthenticationService;


    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;


    @Autowired
    private Validator validator;

    private MockMvc restAccountMockMvc;

    private MockMvc jwtAuthenticationMockMvc;

    private Account account1;
    private Account account2;


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

        final JwtAuthenticationResource jwtAuthenticationResource = new JwtAuthenticationResource(jwtAuthenticationService);
        this.jwtAuthenticationMockMvc = MockMvcBuilders.standaloneSetup(jwtAuthenticationResource)
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

    public static Account createEntity1() {
        Account account = new Account()
                .accountNumber(DEFAULT_CARD_NUMBER_1)
                .pin(DEFAULT_PIN_1);
        return account;
    }

    public static Account createEntity2() {
        Account account = new Account()
                .accountNumber(DEFAULT_CARD_NUMBER_2)
                .pin(DEFAULT_PIN_2)
                .expiryTime(ZonedDateTime.now().minusDays(1));
        return account;
    }


    @BeforeEach
    public void initTest() throws Exception {
        accountRepository.deleteByAccountNumber(DEFAULT_CARD_NUMBER_1);
        accountRepository.deleteByAccountNumber(DEFAULT_CARD_NUMBER_2);
        account1 = createEntity1();
        account2 = createEntity2();
        createAccount();

    }

    private void createAccount() throws Exception {
        // Create the Account1
        AccountDTO accountDTO1 = accountMapper.toDto(account1);
        restAccountMockMvc.perform(post("/api/accounts")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(accountDTO1)))
                .andExpect(status().isCreated());

        // Create the Account2
        AccountDTO accountDTO2 = accountMapper.toDto(account2);
        restAccountMockMvc.perform(post("/api/accounts")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(accountDTO2)))
                .andExpect(status().isCreated());
    }

    @Test
    @Transactional
    public void successfullyLoginTest() throws Exception {
        LoginRequestDTO loginRequestDTO;
        loginRequestDTO = new LoginRequestDTO().name(DEFAULT_CARD_NUMBER_1).pin(DEFAULT_PIN_1);
        jwtAuthenticationMockMvc.perform(post("/authenticate")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(loginRequestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void unSuccessfullyLoginTest() throws Exception {
        LoginRequestDTO loginRequestDTO;
        loginRequestDTO = new LoginRequestDTO().name(DEFAULT_CARD_NUMBER_1).pin(DEFAULT_PIN_2);
        jwtAuthenticationMockMvc.perform(post("/authenticate")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(loginRequestDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertEquals("INVALID_CREDENTIALS", result.getResolvedException().getMessage()));
    }

    @Test
    @Transactional
    public void disabledAccountAfter3UnSuccessfullyLoginTest() throws Exception {
        LoginRequestDTO loginRequestDTO;
        loginRequestDTO = new LoginRequestDTO().name(DEFAULT_CARD_NUMBER_1).pin(DEFAULT_PIN_2);
        jwtAuthenticationMockMvc.perform(post("/authenticate")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(loginRequestDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertEquals("INVALID_CREDENTIALS", result.getResolvedException().getMessage()));

        loginRequestDTO = new LoginRequestDTO().name(DEFAULT_CARD_NUMBER_1).pin(DEFAULT_PIN_2);
        jwtAuthenticationMockMvc.perform(post("/authenticate")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(loginRequestDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertEquals("INVALID_CREDENTIALS", result.getResolvedException().getMessage()));
        loginRequestDTO = new LoginRequestDTO().name(DEFAULT_CARD_NUMBER_1).pin(DEFAULT_PIN_2);
        jwtAuthenticationMockMvc.perform(post("/authenticate")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(loginRequestDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertEquals("INVALID_CREDENTIALS", result.getResolvedException().getMessage()));
        loginRequestDTO = new LoginRequestDTO().name(DEFAULT_CARD_NUMBER_1).pin(DEFAULT_PIN_2);
        jwtAuthenticationMockMvc.perform(post("/authenticate")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(loginRequestDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertEquals("User is Disabled", result.getResolvedException().getMessage()));
    }

    @Test
    @Transactional
    public void expiryTimeTest() throws Exception {
        LoginRequestDTO loginRequestDTO;
        loginRequestDTO = new LoginRequestDTO().name(DEFAULT_CARD_NUMBER_2).pin(DEFAULT_PIN_2);
        jwtAuthenticationMockMvc.perform(post("/authenticate")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(loginRequestDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertEquals("User is Disabled", result.getResolvedException().getMessage()));
    }


}

