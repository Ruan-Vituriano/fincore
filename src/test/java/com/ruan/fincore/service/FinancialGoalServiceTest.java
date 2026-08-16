package com.ruan.fincore.service;

import com.ruan.fincore.dto.goal.FinancialGoalProgressResponse;
import com.ruan.fincore.dto.goal.FinancialGoalRequest;
import com.ruan.fincore.dto.goal.FinancialGoalResponse;
import com.ruan.fincore.entity.FinancialGoal;
import com.ruan.fincore.entity.User;
import com.ruan.fincore.enums.Role;
import com.ruan.fincore.exception.ResourceNotFoundException;
import com.ruan.fincore.repository.FinancialGoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialGoalServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private FinancialGoalRepository financialGoalRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private FinancialGoalService financialGoalService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setEmail("test@example.com");
        user.setRole(Role.USER);
    }

    @Test
    void listShouldReturnGoals() {
        FinancialGoal goal = new FinancialGoal();
        goal.setId(UUID.randomUUID());
        goal.setName("Viagem Europa");
        goal.setTargetAmount(BigDecimal.valueOf(10000));
        goal.setCurrentAmount(BigDecimal.valueOf(2000));
        goal.setUser(user);

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(financialGoalRepository.findByUserIdOrderByName(USER_ID)).thenReturn(List.of(goal));

        List<FinancialGoalResponse> response = financialGoalService.list("test@example.com");

        assertThat(response).hasSize(1);
        assertThat(response.get(0).name()).isEqualTo("Viagem Europa");
        assertThat(response.get(0).targetAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
    }

    @Test
    void createShouldSaveGoalWithDefaultCurrentAmount() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(financialGoalRepository.save(any(FinancialGoal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FinancialGoalRequest request = new FinancialGoalRequest("Reserva de emergência",
                BigDecimal.valueOf(5000), null, LocalDate.of(2027, 1, 1));
        FinancialGoalResponse response = financialGoalService.create("test@example.com", request);

        assertThat(response.name()).isEqualTo("Reserva de emergência");
        assertThat(response.currentAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.targetAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
    }

    @Test
    void createShouldSaveGoalWithProvidedCurrentAmount() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(financialGoalRepository.save(any(FinancialGoal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FinancialGoalRequest request = new FinancialGoalRequest("Carro novo",
                BigDecimal.valueOf(50000), BigDecimal.valueOf(10000), null);
        FinancialGoalResponse response = financialGoalService.create("test@example.com", request);

        assertThat(response.currentAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
    }

    @Test
    void updateShouldModifyGoal() {
        FinancialGoal goal = new FinancialGoal();
        goal.setId(UUID.randomUUID());
        goal.setName("Meta Antiga");
        goal.setTargetAmount(BigDecimal.valueOf(1000));
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setUser(user);

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(financialGoalRepository.findByIdAndUserId(goal.getId(), USER_ID)).thenReturn(Optional.of(goal));

        FinancialGoalRequest request = new FinancialGoalRequest("Meta Atualizada",
                BigDecimal.valueOf(8000), BigDecimal.valueOf(2000), LocalDate.of(2027, 6, 1));
        FinancialGoalResponse response = financialGoalService.update("test@example.com", goal.getId(), request);

        assertThat(response.name()).isEqualTo("Meta Atualizada");
        assertThat(response.targetAmount()).isEqualByComparingTo(BigDecimal.valueOf(8000));
        assertThat(response.currentAmount()).isEqualByComparingTo(BigDecimal.valueOf(2000));
    }

    @Test
    void updateShouldThrowWhenGoalNotFound() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(financialGoalRepository.findByIdAndUserId(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());

        FinancialGoalRequest request = new FinancialGoalRequest("Teste", BigDecimal.valueOf(500), null, null);

        assertThatThrownBy(() -> financialGoalService.update("test@example.com", UUID.randomUUID(), request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Meta financeira não encontrada");
    }

    @Test
    void deleteShouldRemoveGoal() {
        FinancialGoal goal = new FinancialGoal();
        goal.setId(UUID.randomUUID());
        goal.setUser(user);

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(financialGoalRepository.findByIdAndUserId(goal.getId(), USER_ID)).thenReturn(Optional.of(goal));

        financialGoalService.delete("test@example.com", goal.getId());

        verify(financialGoalRepository).delete(goal);
    }

    @Test
    void deleteShouldThrowWhenGoalNotFound() {
        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(financialGoalRepository.findByIdAndUserId(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> financialGoalService.delete("test@example.com", UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Meta financeira não encontrada");
    }

    @Test
    void progressShouldCalculatePercentage() {
        UUID goalId = UUID.randomUUID();
        FinancialGoal goal = new FinancialGoal();
        goal.setId(goalId);
        goal.setName("Viagem Europa");
        goal.setTargetAmount(BigDecimal.valueOf(10000));
        goal.setCurrentAmount(BigDecimal.valueOf(2500));
        goal.setUser(user);

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(financialGoalRepository.findByIdAndUserId(goalId, USER_ID)).thenReturn(Optional.of(goal));

        FinancialGoalProgressResponse response = financialGoalService.progress("test@example.com", goalId);

        assertThat(response.percentageAchieved()).isEqualByComparingTo(BigDecimal.valueOf(25.0000));
        assertThat(response.targetAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(response.currentAmount()).isEqualByComparingTo(BigDecimal.valueOf(2500));
    }

    @Test
    void progressShouldReturnZeroWhenTargetIsZero() {
        UUID goalId = UUID.randomUUID();
        FinancialGoal goal = new FinancialGoal();
        goal.setId(goalId);
        goal.setName("Meta Zerada");
        goal.setTargetAmount(BigDecimal.ZERO);
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setUser(user);

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(financialGoalRepository.findByIdAndUserId(goalId, USER_ID)).thenReturn(Optional.of(goal));

        FinancialGoalProgressResponse response = financialGoalService.progress("test@example.com", goalId);

        assertThat(response.percentageAchieved()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void progressShouldAllowOverOneHundredPercent() {
        UUID goalId = UUID.randomUUID();
        FinancialGoal goal = new FinancialGoal();
        goal.setId(goalId);
        goal.setName("Meta Superada");
        goal.setTargetAmount(BigDecimal.valueOf(5000));
        goal.setCurrentAmount(BigDecimal.valueOf(7500));
        goal.setUser(user);

        when(userService.findByEmail("test@example.com")).thenReturn(user);
        when(financialGoalRepository.findByIdAndUserId(goalId, USER_ID)).thenReturn(Optional.of(goal));

        FinancialGoalProgressResponse response = financialGoalService.progress("test@example.com", goalId);

        assertThat(response.percentageAchieved()).isEqualByComparingTo(BigDecimal.valueOf(150.0000));
    }
}
