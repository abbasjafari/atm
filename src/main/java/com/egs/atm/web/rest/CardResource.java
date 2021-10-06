package com.egs.atm.web.rest;

import com.egs.atm.service.dto.CardDTO;
import com.egs.atm.web.rest.errors.BadRequestAlertException;
import com.egs.atm.web.rest.util.HeaderUtil;
import com.egs.atm.web.rest.util.ResponseUtil;
import com.egs.atm.service.CardService;
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
public class CardResource {

    private final Logger log = LoggerFactory.getLogger(CardResource.class);

    private static final String ENTITY_NAME = "atmCard";

    @Value("${atm.clientApp.name}")
    private String applicationName;

    private final CardService cardService;

    public CardResource(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/cards")
    public ResponseEntity<CardDTO> createCard(@RequestBody CardDTO cardDTO) throws URISyntaxException {
        log.debug("REST request to save Card : {}", cardDTO);
        if (cardDTO.getId() != null) {
            throw new BadRequestAlertException("A new card cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CardDTO result = cardService.save(cardDTO);
        return ResponseEntity.created(new URI("/api/cards/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    @PutMapping("/cards")
    public ResponseEntity<CardDTO> updateCard(@RequestBody CardDTO cardDTO) throws URISyntaxException {
        log.debug("REST request to update Card : {}", cardDTO);
        if (cardDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        CardDTO result = cardService.save(cardDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, cardDTO.getId().toString()))
                .body(result);
    }

    @GetMapping("/cards")
    public List<CardDTO> getAllCards() {
        log.debug("REST request to get all Cards");
        return cardService.findAll();
    }


    @GetMapping("/cards/{id}")
    public ResponseEntity<CardDTO> getCard(@PathVariable Long id) {
        log.debug("REST request to get Card : {}", id);
        Optional<CardDTO> cardDTO = cardService.findOne(id);
        return ResponseUtil.wrapOrNotFound(cardDTO);
    }


    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        log.debug("REST request to delete Card : {}", id);
        cardService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }

}
