package com.easyaccount.server.dto;

import com.easyaccount.server.entity.*;
import lombok.Data;
import java.util.List;

@Data
public class SyncRequest {
    private List<Category> categories;
    private List<Transaction> transactions;
    private List<Tag> tags;
    private List<TransactionTag> transactionTags;
    private List<Budget> budgets;
    private List<RecurringRule> recurringRules;
}
