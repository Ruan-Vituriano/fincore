package com.ruan.fincore.repository;

import com.ruan.fincore.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByUserIdOrderByName(UUID userId);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByNameIgnoreCaseAndUserId(String name, UUID userId);

    boolean existsByNameIgnoreCaseAndUserIdAndIdNot(String name, UUID userId, UUID id);
}
