package com.ruan.fincore.service;

import com.ruan.fincore.dto.investment.AllocationByType;
import com.ruan.fincore.dto.investment.InvestmentRequest;
import com.ruan.fincore.dto.investment.InvestmentResponse;
import com.ruan.fincore.dto.investment.PortfolioSummaryResponse;
import com.ruan.fincore.entity.Investment;
import com.ruan.fincore.entity.User;
import com.ruan.fincore.enums.InvestmentType;
import com.ruan.fincore.exception.ResourceNotFoundException;
import com.ruan.fincore.mapper.InvestmentMapper;
import com.ruan.fincore.repository.InvestmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<InvestmentResponse> list(String email) {
        UUID userId = findUserId(email);
        return investmentRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(InvestmentMapper::toResponse)
                .toList();
    }

    @Transactional
    public InvestmentResponse create(String email, InvestmentRequest request) {
        User user = userService.findByEmail(email);
        Investment investment = new Investment();
        investment.setName(request.name());
        investment.setTicker(request.ticker());
        investment.setType(request.type());
        investment.setAmountInvested(request.amountInvested());
        investment.setCurrentValue(request.currentValue());
        investment.setPurchaseDate(request.purchaseDate());
        investment.setNotes(request.notes());
        investment.setUser(user);
        return InvestmentMapper.toResponse(investmentRepository.save(investment));
    }

    @Transactional
    public InvestmentResponse update(String email, UUID id, InvestmentRequest request) {
        UUID userId = findUserId(email);
        Investment investment = findOwnedInvestment(id, userId);
        investment.setName(request.name());
        investment.setTicker(request.ticker());
        investment.setType(request.type());
        investment.setAmountInvested(request.amountInvested());
        investment.setCurrentValue(request.currentValue());
        investment.setPurchaseDate(request.purchaseDate());
        investment.setNotes(request.notes());
        return InvestmentMapper.toResponse(investment);
    }

    @Transactional
    public void delete(String email, UUID id) {
        UUID userId = findUserId(email);
        Investment investment = findOwnedInvestment(id, userId);
        investmentRepository.delete(investment);
    }

    @Transactional(readOnly = true)
    public PortfolioSummaryResponse summary(String email) {
        UUID userId = findUserId(email);
        List<Investment> investments = investmentRepository.findByUserIdOrderByNameAsc(userId);

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;
        Map<InvestmentType, BigDecimal> byType = new HashMap<>();

        for (Investment i : investments) {
            totalInvested = totalInvested.add(i.getAmountInvested());
            totalCurrentValue = totalCurrentValue.add(i.getCurrentValue());
            byType.merge(i.getType(), i.getCurrentValue(), BigDecimal::add);
        }

        BigDecimal totalReturn = totalCurrentValue.subtract(totalInvested);
        BigDecimal totalReturnPercentage = totalInvested.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : totalReturn.divide(totalInvested, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        Map<String, AllocationByType> allocation = new HashMap<>();
        for (Map.Entry<InvestmentType, BigDecimal> entry : byType.entrySet()) {
            BigDecimal pct = totalCurrentValue.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : entry.getValue().divide(totalCurrentValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            allocation.put(entry.getKey().name(), new AllocationByType(entry.getValue(), pct));
        }

        return new PortfolioSummaryResponse(totalInvested, totalCurrentValue, totalReturn, totalReturnPercentage, allocation);
    }

    private UUID findUserId(String email) {
        return userService.findByEmail(email).getId();
    }

    private Investment findOwnedInvestment(UUID id, UUID userId) {
        return investmentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Investimento não encontrado"));
    }
}
