package com.ruan.fincore.mapper;

import com.ruan.fincore.dto.transaction.TransactionResponse;
import com.ruan.fincore.entity.Transaction;

public final class TransactionMapper {

    private TransactionMapper() {
    }

    public static TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getType(),
                transaction.getCategory().getId(),
                transaction.getCategory().getName(),
                transaction.getAccount().getId(),
                transaction.getAccount().getName(),
                transaction.getUser().getId(),
                transaction.getNotes(),
                transaction.getIsRecurring(),
                transaction.getInstallmentNumber(),
                transaction.getTotalInstallments(),
                transaction.getParentTransaction() != null ? transaction.getParentTransaction().getId() : null,
                transaction.getCreatedAt()
        );
    }
}
