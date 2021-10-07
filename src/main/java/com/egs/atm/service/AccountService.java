package com.egs.atm.service;

import com.egs.atm.domain.Account;
import com.egs.atm.repository.AccountRepository;
import com.egs.atm.service.dto.AccountDTO;
import com.egs.atm.service.mapper.AccountMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    public AccountDTO save(AccountDTO accountDTO) {
        log.debug("Request to save Account : {}", accountDTO);
        Account account = accountMapper.toEntity(accountDTO);
        account.setPin(new BCryptPasswordEncoder().encode(account.getPin()));
        account = accountRepository.save(account);
        AccountDTO result = accountMapper.toDto(account);
        return result;
    }


    @Transactional(readOnly = true)
    public List<AccountDTO> findAll() {
        log.debug("Request to get all Accounts");
        return accountRepository.findAll().stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toCollection(LinkedList::new));
    }


    @Transactional(readOnly = true)
    public Optional<AccountDTO> findOne(Long id) {
        log.debug("Request to get Account : {}", id);
        return accountRepository.findById(id)
                .map(accountMapper::toDto);
    }

    public void delete(Long id) {
        log.debug("Request to delete Account : {}", id);
        accountRepository.deleteById(id);
    }

}

