package com.ruan.fincore.service;

import com.ruan.fincore.dto.account.AccountRequest;
import com.ruan.fincore.dto.account.AccountResponse;
import com.ruan.fincore.entity.Account;
import com.ruan.fincore.entity.User;
import com.ruan.fincore.exception.BusinessException;
import com.ruan.fincore.exception.ResourceNotFoundException;
import com.ruan.fincore.mapper.AccountMapper;
import com.ruan.fincore.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<AccountResponse> list(String email) {
        UUID userId = findUserId(email);
        return accountRepository.findByUserIdOrderByName(userId).stream()
                .map(AccountMapper::toResponse)
                .toList();
    }

    @Transactional
    public AccountResponse create(String email, AccountRequest request) {
        User user = userService.findByEmail(email);
        validateUniqueName(user.getId(), request.name(), null);
        Account account = new Account();
        account.setName(request.name());
        account.setType(request.type());
        account.setBalance(request.balance() != null ? request.balance() : BigDecimal.ZERO);
        account.setUser(user);
        return AccountMapper.toResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse update(String email, UUID id, AccountRequest request) {
        UUID userId = findUserId(email);
        validateUniqueName(userId, request.name(), id);
        Account account = findOwnedAccount(id, userId);
        account.setName(request.name());
        account.setType(request.type());
        account.setBalance(request.balance());
        return AccountMapper.toResponse(account);
    }

    @Transactional
    public void delete(String email, UUID id) {
        Account account = findOwnedAccount(id, findUserId(email));
        accountRepository.delete(account);
    }

    private UUID findUserId(String email) {
        return userService.findByEmail(email).getId();
    }

    private Account findOwnedAccount(UUID id, UUID userId) {
        return accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada"));
    }

    private void validateUniqueName(UUID userId, String name, UUID id) {
        boolean exists = id == null
                ? accountRepository.existsByNameIgnoreCaseAndUserId(name, userId)
                : accountRepository.existsByNameIgnoreCaseAndUserIdAndIdNot(name, userId, id);
        if (exists) {
            throw new BusinessException("Já existe uma conta com este nome");
        }
    }
}
