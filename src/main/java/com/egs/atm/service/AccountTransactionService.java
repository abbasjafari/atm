package com.egs.atm.service;

import com.egs.atm.domain.Account;
import com.egs.atm.domain.AccountTransaction;
import com.egs.atm.domain.enumration.TransactionType;
import com.egs.atm.repository.AccountRepository;
import com.egs.atm.repository.AccountTransactionRepository;
import com.egs.atm.web.rest.errors.BadRequestAlertException;
import com.egs.atm.web.rest.errors.LogicAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@Transactional
public class AccountTransactionService {

    private final Logger log = LoggerFactory.getLogger(AccountTransactionService.class);

    private final AccountTransactionRepository accountTransactionRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;


    public AccountTransactionService(AccountTransactionRepository accountTransactionRepository, AccountRepository accountRepository, AccountService accountService) {
        this.accountTransactionRepository = accountTransactionRepository;
        this.accountRepository = accountRepository;
        this.accountService = accountService;
    }

    public BigDecimal depositAccount(BigDecimal amount) throws Exception {
        log.debug("request to deposit account : "+ amount +" for card number : "+ accountService.findUsername());
        Account account = accountService.findAccount();
        BigDecimal balance = calculateBalance(account);
        AccountTransaction accountTransaction = new AccountTransaction()
                .account(account)
                .amount(amount)
                .balance(balance.add(amount))
                .parentAccountTransaction(account.getLastAccountTransaction())
                .transactionType(TransactionType.DEPOSIT);
        accountTransactionRepository.save(accountTransaction);
        accountTransaction.setHashData(calculateHash(accountTransaction));
        accountTransactionRepository.save(accountTransaction);
        account.setLastAccountTransaction(accountTransaction);
        accountRepository.save(account);

        return accountTransaction.getBalance();
    }

    /**
     * withdraw amount from account
     * */
    public BigDecimal withdrawalAccount(BigDecimal amount) throws Exception {
        log.debug("request to withdrawal account : "+ amount+" for card number : "+ accountService.findUsername());

        Account account = accountService.findAccount();
        BigDecimal balance = calculateBalance(account);
        if (balance.compareTo(amount) < 0) {
            throw new BadRequestAlertException("Amount is not enough","Account","notEnough");
        }
        AccountTransaction accountTransaction = new AccountTransaction()
                .account(account)
                .amount(amount)
                .balance(balance.subtract(amount))
                .parentAccountTransaction(account.getLastAccountTransaction())
                .transactionType(TransactionType.WITHDRAWAL);
        accountTransactionRepository.save(accountTransaction);
        accountTransaction.setHashData(calculateHash(accountTransaction));
        accountTransactionRepository.save(accountTransaction);
        account.setLastAccountTransaction(accountTransaction);
        accountRepository.save(account);

        return accountTransaction.getBalance();
    }

    /**
     * return current account balance
     * */
    @Transactional(readOnly = true)
    public BigDecimal checkBalance() throws Exception {
        log.debug(" request to  check balance for card number : "+ accountService.findUsername());
        Account account = accountService.findAccount();
        return calculateBalance(account);

    }

    /**
     * calculating balance for current user
     * @throws LogicAlertException
     * */
    public BigDecimal calculateBalance(Account account) throws Exception {
        log.debug(" request to  calculate balance for card number : "+ account.getAccountNumber());
        if (account.getLastAccountTransaction() != null) {
            AccountTransaction accountTransaction = account.getLastAccountTransaction();
            String hash = calculateHash(accountTransaction);
            if (!hash.equals(accountTransaction.getHashData()))
                throw new LogicAlertException("Data is invalid");
            else return accountTransaction.getBalance();
        } else return BigDecimal.ZERO;
    }

    /**
     * calculating account transaction hash with md5 algorithm
     * see also {@link AccountTransaction}
     * */
    public String calculateHash(AccountTransaction accountTransaction) throws NoSuchAlgorithmException {
        log.debug(" request to  calculate hash for card number : "+ accountTransaction.getAccount().getAccountNumber());
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update((accountTransaction.toString() + accountTransaction.getLastAccountTransactionHash()).getBytes());
        byte[] digest = md.digest();
        return DatatypeConverter
                .printHexBinary(digest).toUpperCase();
    }
}

