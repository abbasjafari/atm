package com.egs.atm.service.mapper;

import com.egs.atm.service.dto.CardDTO;
import com.egs.atm.domain.Card;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {})
public interface CardMapper extends EntityMapper<CardDTO, Card> {


    default Card fromId(Long id) {
        if (id == null) {
            return null;
        }
        Card card = new Card();
        card.setId(id);
        return card;
    }
}
