package com.egs.atm.service;

import com.egs.atm.domain.Account;
import com.egs.atm.repository.AccountRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class JwtUserDetailsService implements UserDetailsService {
private final AccountRepository accountRepository;

    public JwtUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(username.equals("admin")){
            ArrayList<GrantedAuthority> authority = new ArrayList<>();
            authority.add(new SimpleGrantedAuthority("admin"));
            return new User("admin","$2a$10$/1LwPVVOZVwyLwr4x3cKH.bpJFKtEZCCfepx8rOh/m0BSc2JUoXMm", authority);
        }
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(username);
        if(accountOptional.isPresent()){
            Account account = accountOptional.get();
            if (account.getUnsuccessfulCount()>=3) {
                throw new DisabledException("User is Disabled");
            }
            return new User(account.getAccountNumber(),account.getPin(),
                    new ArrayList<>());
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
}