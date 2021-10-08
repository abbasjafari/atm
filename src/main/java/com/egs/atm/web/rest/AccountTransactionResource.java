package com.egs.atm.web.rest;

import com.egs.atm.service.AccountService;
import com.egs.atm.service.AccountTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * ATM APIs
 * */
@RestController
@RequestMapping("/api")
public class AccountTransactionResource {

    private final Logger log = LoggerFactory.getLogger(AccountTransactionResource.class);

    private final AccountTransactionService accountTransactionService;
    private final AccountService accountService;



    public AccountTransactionResource(AccountTransactionService accountTransactionService, AccountService accountService) {
        this.accountTransactionService = accountTransactionService;
        this.accountService = accountService;
    }

    /**
     * rest service for putting some money in account
     * */
    @PostMapping("/account-deposit")
    public ResponseEntity<BigDecimal> depositAccount(@RequestHeader MultiValueMap<String, String> headers, @RequestParam BigDecimal amount) throws Exception {
        log.debug("REST request to deposit account : "+ amount +" for card number : "+ accountService.findUsername());

        BigDecimal result = accountTransactionService.depositAccount(amount);
        return ResponseEntity.ok(result);
    }


    /**
     * rest service for getting some money from account
     * */
    @PostMapping("/account-withdrawal")
    public ResponseEntity<BigDecimal> withdrawalAccount(@RequestParam BigDecimal amount) throws Exception {
        log.debug("REST request to withdrawal account : "+ amount+" for card number : "+ accountService.findUsername());
        BigDecimal result = accountTransactionService.withdrawalAccount(amount);
        return ResponseEntity.ok(result);
    }

    /**
     * rest service for getting  money amount of an account
     * */
    @PostMapping("/account-balance")
    public ResponseEntity<BigDecimal> checkBalance() throws Exception {
        log.debug("REST request to  check balance for card number : "+ accountService.findUsername());
        BigDecimal result = accountTransactionService.checkBalance();
        return ResponseEntity.ok(result);
    }
}
