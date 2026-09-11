package com.easyaccount.server.repository;

import com.easyaccount.server.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Budget findFirstByCategoryIdIsNull();
}
