package com.egs.atm.service;

import com.egs.atm.domain.Account;
import com.egs.atm.repository.AccountRepository;
import com.egs.atm.service.dto.AccountDTO;
import com.egs.atm.service.mapper.AccountMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountService {

    private final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;

    private final AccountMapper accountMapper;


    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }


    /**
     * saving account and encoded password
     * see also {@link AccountDTO}
     */
    public AccountDTO save(AccountDTO accountDTO) {
        log.debug("Request to save Account : {}", accountDTO);
        Account account = accountMapper.toEntity(accountDTO);
        if (account.getId() != null) {
            Optional<Account> optionalAccount = accountRepository.findById(account.getId());
            if (optionalAccount.isPresent()) {
                account.setLastAccountTransaction(optionalAccount.get().getLastAccountTransaction());
                if (account.getPin() != null) {
                    account.setPin(new BCryptPasswordEncoder().encode(account.getPin()));
                }else {
                    account.setPin(optionalAccount.get().getPin());
                }
            }

        }else
            account.setPin(new BCryptPasswordEncoder().encode(account.getPin()));
        if(account.getExpiryTime()==null)
            account.setExpiryTime(ZonedDateTime.now().plusYears(3));
        account = accountRepository.save(account);
        AccountDTO result = accountMapper.toDto(account);
        return result;
    }

    /**
     * getting all accounts converted to AccountDTO
     * see also {@link AccountDTO}
     */
    @Transactional(readOnly = true)
    public List<AccountDTO> findAll() {
        log.debug("Request to get all Accounts");
        return accountRepository.findAll().stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * finding account base on account id
     */
    @Transactional(readOnly = true)
    public Optional<AccountDTO> findOne(Long id) {
        log.debug("Request to get Account : {}", id);
        return accountRepository.findById(id)
                .map(accountMapper::toDto);
    }

    /**
     * deleting account base on account id
     */
    public void delete(Long id) {
        log.debug("Request to delete Account : {}", id);
        accountRepository.deleteById(id);
    }

    /**
     * finding username base on session info and then finding
     * related account by account number
     */
    @Transactional(readOnly = true)
    public Account findAccount() {
        String username = findUsername();
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(username);
        if (accountOptional.isPresent()) {
            return accountOptional.get();
        } else throw new UsernameNotFoundException("User not found with username: " + username);
    }

    /**
     * getting username from session
     */
    public String findUsername() {
        String username;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return username;
    }

}

