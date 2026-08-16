package com.ruan.fincore.mapper;

import com.ruan.fincore.dto.account.AccountResponse;
import com.ruan.fincore.entity.Account;

public final class AccountMapper {

    private AccountMapper() {
    }

    public static AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getBalance(),
                account.getUser().getId(),
                account.getCreatedAt()
        );
    }
}
