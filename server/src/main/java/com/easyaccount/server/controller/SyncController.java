package com.easyaccount.server.controller;

import com.easyaccount.server.dto.*;
import com.easyaccount.server.entity.*;
import com.easyaccount.server.repository.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SyncController {

    private final CategoryRepository catRepo;
    private final TransactionRepository txRepo;
    private final TagRepository tagRepo;
    private final BudgetRepository budgetRepo;
    private final RecurringRuleRepository ruleRepo;

    public SyncController(CategoryRepository catRepo, TransactionRepository txRepo,
                           TagRepository tagRepo, BudgetRepository budgetRepo,
                           RecurringRuleRepository ruleRepo) {
        this.catRepo = catRepo;
        this.txRepo = txRepo;
        this.tagRepo = tagRepo;
        this.budgetRepo = budgetRepo;
        this.ruleRepo = ruleRepo;
    }

    /** 接收 Android 上传的数据，返回服务器全量数据 */
    @PostMapping("/sync")
    public SyncResponse sync(@RequestBody SyncRequest req) {
        // 保存上传的数据
        if (req.getCategories() != null) catRepo.saveAll(req.getCategories());
        if (req.getTransactions() != null) txRepo.saveAll(req.getTransactions());
        if (req.getTags() != null) tagRepo.saveAll(req.getTags());
        if (req.getBudgets() != null) budgetRepo.saveAll(req.getBudgets());
        if (req.getRecurringRules() != null) ruleRepo.saveAll(req.getRecurringRules());

        // 返回服务器全量数据
        SyncResponse resp = new SyncResponse();
        resp.setCategories(catRepo.findAll());
        resp.setTransactions(txRepo.findAll());
        resp.setTags(tagRepo.findAll());
        resp.setBudgets(budgetRepo.findAll());
        resp.setRecurringRules(ruleRepo.findAll());
        resp.setServerTimeMs(System.currentTimeMillis());
        return resp;
    }

    /** 统计查询 */
    @GetMapping("/stats/category")
    public List<Object[]> getCategoryStats(
            @RequestParam Integer type,
            @RequestParam Long startMs,
            @RequestParam Long endMs) {
        return txRepo.sumByCategoryAndDateRange(type, startMs, endMs);
    }

    @GetMapping("/categories")
    public List<Category> getCategories() {
        return catRepo.findAll();
    }

    @GetMapping("/transactions/range")
    public List<Transaction> getTransactions(
            @RequestParam Long startMs,
            @RequestParam Long endMs) {
        return txRepo.findByDateMsBetween(startMs, endMs);
    }
}
