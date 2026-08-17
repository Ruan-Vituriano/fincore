package com.ruan.fincore.service;

import com.ruan.fincore.dto.account.AccountRequest;
import com.ruan.fincore.dto.account.AccountResponse;
import com.ruan.fincore.entity.Account;
import com.ruan.fincore.entity.User;
import com.ruan.fincore.enums.AccountType;
import com.ruan.fincore.enums.Role;
import com.ruan.fincore.exception.BusinessException;
import com.ruan.fincore.exception.ResourceNotFoundException;
import com.ruan.fincore.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AccountService accountService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setEmail("test@example.com");
        user.setRole(Role.USER);
    }

    @Test
    void listShouldReturnUserAccounts() {
        Account account = account("Nubank", AccountType.CREDIT_CARD, BigDecimal.valueOf(-500), user);
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(accountRepository.findByUserIdOrderByName(USER_ID)).thenReturn(List.of(account));

        List<AccountResponse> response = accountService.list("test@example.com");

        assertThat(response).hasSize(1);
        assertThat(response.get(0).name()).isEqualTo("Nubank");
        assertThat(response.get(0).type()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(response.get(0).userId()).isEqualTo(USER_ID);
    }

    @Test
    void createShouldReturnCreatedAccount() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(accountRepository.existsByNameIgnoreCaseAndUserId("Nubank", USER_ID)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = accountService.create("test@example.com",
                new AccountRequest("Nubank", AccountType.CREDIT_CARD, BigDecimal.valueOf(-500)));

        assertThat(response.name()).isEqualTo("Nubank");
        assertThat(response.type()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(response.balance()).isEqualByComparingTo(BigDecimal.valueOf(-500));
        assertThat(response.userId()).isEqualTo(USER_ID);
    }

    @Test
    void createShouldDefaultBalanceToZeroWhenNull() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(accountRepository.existsByNameIgnoreCaseAndUserId("Itaucard", USER_ID)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account saved = invocation.getArgument(0);
            assertThat(saved.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            return saved;
        });

        AccountResponse response = accountService.create("test@example.com",
                new AccountRequest("Itaucard", AccountType.CREDIT_CARD, null));

        assertThat(response.balance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void createShouldThrowBusinessExceptionWhenNameAlreadyExists() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(accountRepository.existsByNameIgnoreCaseAndUserId("Nubank", USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> accountService.create("test@example.com",
                new AccountRequest("Nubank", AccountType.CREDIT_CARD, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Já existe uma conta com este nome");
    }

    @Test
    void updateShouldUpdateOwnedAccount() {
        Account account = account("Nubank", AccountType.CREDIT_CARD, BigDecimal.valueOf(-500), user);
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(accountRepository.findByIdAndUserId(account.getId(), USER_ID)).thenReturn(Optional.of(account));
        when(accountRepository.existsByNameIgnoreCaseAndUserIdAndIdNot("Inter", USER_ID, account.getId()))
                .thenReturn(false);

        AccountResponse response = accountService.update("test@example.com", account.getId(),
                new AccountRequest("Inter", AccountType.CHECKING, BigDecimal.valueOf(1000)));

        assertThat(response.name()).isEqualTo("Inter");
        assertThat(response.type()).isEqualTo(AccountType.CHECKING);
        assertThat(response.balance()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(account.getName()).isEqualTo("Inter");
    }

    @Test
    void updateShouldThrowNotFoundWhenAccountIsNotOwned() {
        UUID otherId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(accountRepository.findByIdAndUserId(otherId, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.update("test@example.com", otherId,
                new AccountRequest("Inter", AccountType.CHECKING, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteShouldDeleteOwnedAccount() {
        Account account = account("Nubank", AccountType.CREDIT_CARD, BigDecimal.ZERO, user);
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(accountRepository.findByIdAndUserId(account.getId(), USER_ID)).thenReturn(Optional.of(account));

        accountService.delete("test@example.com", account.getId());

        verify(accountRepository).delete(account);
    }

    @Test
    void deleteShouldThrowNotFoundWhenAccountIsNotOwned() {
        UUID otherId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(accountRepository.findByIdAndUserId(otherId, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.delete("test@example.com", otherId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Account account(String name, AccountType type, BigDecimal balance, User owner) {
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setName(name);
        account.setType(type);
        account.setBalance(balance);
        account.setUser(owner);
        return account;
    }
}
