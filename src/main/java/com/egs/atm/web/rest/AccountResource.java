package com.egs.atm.web.rest;

import com.egs.atm.service.AccountService;
import com.egs.atm.service.dto.AccountDTO;
import com.egs.atm.web.rest.errors.BadRequestAlertException;
import com.egs.atm.web.rest.util.HeaderUtil;
import com.egs.atm.web.rest.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AccountResource {

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);

    private static final String ENTITY_NAME = "Account";

    @Value("${atm.clientApp.name}")
    private String applicationName;

    private final AccountService accountService;

    public AccountResource(AccountService accountService) throws URISyntaxException {
        this.accountService = accountService;
      createAccount(new AccountDTO().name("1111-1111-1111-1111").pin("1234"));
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountDTO> createAccount(@RequestBody AccountDTO accountDTO) throws URISyntaxException {
        log.debug("REST request to save Account : {}", accountDTO);
        if (accountDTO.getId() != null) {
            throw new BadRequestAlertException("A new account cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AccountDTO result = accountService.save(accountDTO);
        return ResponseEntity.created(new URI("/api/accounts/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    @PutMapping("/accounts")
    public ResponseEntity<AccountDTO> updateAccount(@RequestBody AccountDTO accountDTO) throws URISyntaxException {
        log.debug("REST request to update Account : {}", accountDTO);
        if (accountDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AccountDTO result = accountService.save(accountDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, accountDTO.getId().toString()))
                .body(result);
    }

    @GetMapping("/accounts")
    public List<AccountDTO> getAllAccounts() {
        log.debug("REST request to get all Accounts");
        return accountService.findAll();
    }


    @GetMapping("/accounts/{id}")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable Long id) {
        log.debug("REST request to get Account : {}", id);
        Optional<AccountDTO> accountDTO = accountService.findOne(id);
        return ResponseUtil.wrapOrNotFound(accountDTO);
    }


    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        log.debug("REST request to delete Account : {}", id);
        accountService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }

}
