package com.egs.atm.service;

import com.egs.atm.repository.CardRepository;
import com.egs.atm.service.dto.CardDTO;
import com.egs.atm.service.mapper.CardMapper;
import com.egs.atm.domain.Card;
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
public class CardService {

    private final Logger log = LoggerFactory.getLogger(CardService.class);

    private final CardRepository cardRepository;

    private final CardMapper cardMapper;


    public CardService(CardRepository cardRepository, CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.cardMapper = cardMapper;
    }


    public CardDTO save(CardDTO cardDTO) {
        log.debug("Request to save Card : {}", cardDTO);
        Card card = cardMapper.toEntity(cardDTO);
        card.setPin(new BCryptPasswordEncoder().encode(card.getPin()));
        card = cardRepository.save(card);
        CardDTO result = cardMapper.toDto(card);
        return result;
    }


    @Transactional(readOnly = true)
    public List<CardDTO> findAll() {
        log.debug("Request to get all Cards");
        return cardRepository.findAll().stream()
                .map(cardMapper::toDto)
                .collect(Collectors.toCollection(LinkedList::new));
    }


    @Transactional(readOnly = true)
    public Optional<CardDTO> findOne(Long id) {
        log.debug("Request to get Card : {}", id);
        return cardRepository.findById(id)
                .map(cardMapper::toDto);
    }

    public void delete(Long id) {
        log.debug("Request to delete Card : {}", id);
        cardRepository.deleteById(id);
    }

}

