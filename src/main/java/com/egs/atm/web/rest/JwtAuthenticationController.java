package com.egs.atm.web.rest;

import com.egs.atm.service.JwtAuthenticationService;
import com.egs.atm.service.dto.AccountDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
public class JwtAuthenticationController {


    private final JwtAuthenticationService jwtAuthenticationService;

    public JwtAuthenticationController(JwtAuthenticationService jwtAuthenticationService) {
        this.jwtAuthenticationService = jwtAuthenticationService;
    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AccountDTO authenticationRequest) throws Exception {

        return ResponseEntity.ok(jwtAuthenticationService.createAuthenticationToken(authenticationRequest));
    }
}