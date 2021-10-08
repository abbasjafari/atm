package com.egs.atm.web.rest;

import com.egs.atm.service.AccountService;
import com.egs.atm.service.AccountTransactionalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * ATM APIs
 * */
@RestController
@RequestMapping("/api")
public class AccountTransactionalResource {

    private final Logger log = LoggerFactory.getLogger(AccountTransactionalResource.class);

    private final AccountTransactionalService accountTransactionalService;
    private final AccountService accountService;



    public AccountTransactionalResource(AccountTransactionalService accountTransactionalService, AccountService accountService) {
        this.accountTransactionalService = accountTransactionalService;
        this.accountService = accountService;
    }

    /**
     * rest service for putting some money in account
     * */
    @PostMapping("/account-deposit")
    public ResponseEntity<BigDecimal> depositAccount(@RequestParam BigDecimal amount) throws Exception {
        log.debug("REST request to deposit account : "+ amount +" for card number : "+ accountService.findUsername());

        BigDecimal result = accountTransactionalService.depositAccount(amount);
        return ResponseEntity.ok(result);
    }


    /**
     * rest service for getting some money from account
     * */
    @PostMapping("/account-withdrawal")
    public ResponseEntity<BigDecimal> withdrawalAccount(@RequestParam BigDecimal amount) throws Exception {
        log.debug("REST request to withdrawal account : "+ amount+" for card number : "+ accountService.findUsername());
        BigDecimal result = accountTransactionalService.withdrawalAccount(amount);
        return ResponseEntity.ok(result);
    }

    /**
     * rest service for getting  money amount of an account
     * */
    @PostMapping("/account-balance")
    public ResponseEntity<BigDecimal> checkBalance() throws Exception {
        log.debug("REST request to  check balance for card number : "+ accountService.findUsername());
        BigDecimal result =accountTransactionalService.checkBalance();
        return ResponseEntity.ok(result);
    }
}
