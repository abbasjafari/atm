package com.egs.atm.web.rest;

import com.egs.atm.service.JwtAuthenticationService;
import com.egs.atm.service.dto.AccountDTO;
import com.egs.atm.service.dto.LoginRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
public class JwtAuthenticationResource {


    private final JwtAuthenticationService jwtAuthenticationService;

    public JwtAuthenticationResource(JwtAuthenticationService jwtAuthenticationService) {
        this.jwtAuthenticationService = jwtAuthenticationService;
    }

    /**
     * rest service for account authentication
     * */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequestDTO authenticationRequest) throws Exception {

        return ResponseEntity.ok(jwtAuthenticationService.createAuthenticationToken(authenticationRequest));
    }
}