package com.easyaccount.server.repository;

import com.easyaccount.server.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByDateMsBetween(Long startMs, Long endMs);

    @Query("SELECT t.categoryId, SUM(t.amount) FROM Transaction t WHERE t.type = :type AND t.dateMs >= :start AND t.dateMs <= :end GROUP BY t.categoryId")
    List<Object[]> sumByCategoryAndDateRange(Integer type, Long start, Long end);
}
