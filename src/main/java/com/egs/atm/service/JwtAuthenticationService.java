package com.egs.atm.service;

import com.egs.atm.config.JwtTokenUtil;
import com.egs.atm.repository.AccountRepository;
import com.egs.atm.service.dto.AccountDTO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class JwtAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsService userDetailsService;
    private final AccountRepository accountRepository;

    public JwtAuthenticationService(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, JwtUserDetailsService userDetailsService, AccountRepository accountRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.accountRepository = accountRepository;
    }


    public String createAuthenticationToken(AccountDTO authenticationRequest) throws Exception {

        authenticate(authenticationRequest.getAccountNumber(), authenticationRequest.getPin());

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getAccountNumber());

        return jwtTokenUtil.generateToken(userDetails);

    }

    @Transactional
    void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            accountRepository.findByAccountNumber(username).ifPresent(account -> account.setUnsuccessfulCount(0));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            accountRepository.findByAccountNumber(username).ifPresent(account -> {
                account.setUnsuccessfulCount(account.getUnsuccessfulCount() + 1);
                accountRepository.save(account);
            });
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
