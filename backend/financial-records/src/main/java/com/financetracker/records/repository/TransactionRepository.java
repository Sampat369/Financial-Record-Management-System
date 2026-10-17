package com.financetracker.records.repository;

import com.financetracker.records.model.Transaction;
import com.financetracker.records.model.TransactionType;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> findByTypeOrderByTransactionDateDescIdDesc(TransactionType type);

    @Query("SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC, t.id DESC")
    List<Transaction> findByDateRange(LocalDate startDate, LocalDate endDate);
}
