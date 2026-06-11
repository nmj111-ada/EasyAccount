package com.easyaccount.app.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.easyaccount.app.data.AppDatabase;
import com.easyaccount.app.data.dao.BudgetDao;
import com.easyaccount.app.data.entity.Budget;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetRepository {

    private final BudgetDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public BudgetRepository(Context context) {
        dao = AppDatabase.getInstance(context).budgetDao();
    }

    public void insert(Budget budget) {
        executor.execute(() -> dao.insert(budget));
    }

    public void update(Budget budget) {
        executor.execute(() -> dao.update(budget));
    }

    public void delete(Budget budget) {
        executor.execute(() -> dao.delete(budget));
    }

    public LiveData<Budget> getTotalBudget() {
        return dao.getTotalBudget();
    }

    public Budget getTotalBudgetSync() {
        return dao.getTotalBudgetSync();
    }

    public LiveData<Budget> getCategoryBudget(long categoryId) {
        return dao.getCategoryBudget(categoryId);
    }

    public LiveData<List<Budget>> getAllBudgets() {
        return dao.getAllBudgets();
    }
}
