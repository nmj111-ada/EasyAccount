package com.easyaccount.server.repository;

import com.easyaccount.server.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecurringRuleRepository extends JpaRepository<RecurringRule, Long> {
    List<RecurringRule> findByEnabledTrueAndNextDateMsLessThanEqual(Long nowMs);
}
