package com.egs.atm.web.rest;

import com.egs.atm.service.AccountTransactionalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
public class AccountTransactionalResource {

    private final Logger log = LoggerFactory.getLogger(AccountTransactionalResource.class);

    private final AccountTransactionalService accountTransactionalService;

    private static final String ENTITY_NAME = "atmAccount";

    @Value("${atm.clientApp.name}")
    private String applicationName;


    public AccountTransactionalResource(AccountTransactionalService accountTransactionalService) {
        this.accountTransactionalService = accountTransactionalService;
    }
    @PostMapping("/account-deposit")
    public ResponseEntity<BigDecimal> depositAccount(@RequestParam BigDecimal amount) {
        BigDecimal result = accountTransactionalService.depositAccount(amount);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/account-withdrawal")
    public ResponseEntity<BigDecimal> withdrawalAccount(@RequestParam BigDecimal amount) throws Exception {
        BigDecimal result = accountTransactionalService.withdrawalAccount(amount);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/account-balance")
    public ResponseEntity<BigDecimal> checkBalance() {
        BigDecimal result =accountTransactionalService.checkBalance();
        return ResponseEntity.ok(result);
    }
}
