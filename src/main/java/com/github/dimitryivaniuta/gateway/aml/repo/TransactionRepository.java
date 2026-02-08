package com.github.dimitryivaniuta.gateway.aml.repo;

import com.github.dimitryivaniuta.gateway.aml.domain.Transaction;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for transactions. */
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {}
