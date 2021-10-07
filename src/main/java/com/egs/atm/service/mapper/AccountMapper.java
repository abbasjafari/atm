package com.egs.atm.service.mapper;

import com.egs.atm.domain.Account;
import com.egs.atm.service.dto.AccountDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {})
public interface AccountMapper extends EntityMapper<AccountDTO, Account> {


    default Account fromId(Long id) {
        if (id == null) {
            return null;
        }
        Account account = new Account();
        account.setId(id);
        return account;
    }
}
