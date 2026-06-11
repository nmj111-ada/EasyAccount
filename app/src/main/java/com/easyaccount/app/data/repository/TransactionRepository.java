package com.easyaccount.app.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.easyaccount.app.data.AppDatabase;
import com.easyaccount.app.data.dao.TransactionDao;
import com.easyaccount.app.data.entity.CategoryTotal;
import com.easyaccount.app.data.entity.MonthlyTotal;
import com.easyaccount.app.data.entity.Transaction;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionRepository {

    private final TransactionDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public TransactionRepository(Context context) {
        dao = AppDatabase.getInstance(context).transactionDao();
    }

    public void insert(Transaction transaction) {
        executor.execute(() -> dao.insert(transaction));
    }

    /** 插入并回调返回ID（用于后续关联标签） */
    public void insert(Transaction transaction, InsertCallback callback) {
        executor.execute(() -> {
            long id = dao.insert(transaction);
            callback.onInserted(id);
        });
    }

    public interface InsertCallback {
        void onInserted(long id);
    }

    public void update(Transaction transaction) {
        executor.execute(() -> dao.update(transaction));
    }

    public void delete(Transaction transaction) {
        executor.execute(() -> dao.delete(transaction));
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return dao.getAllTransactions();
    }

    public LiveData<List<Transaction>> getTransactionsByDateRange(long startMs, long endMs) {
        return dao.getTransactionsByDateRange(startMs, endMs);
    }

    public LiveData<List<Transaction>> getTransactionsByCategory(long categoryId) {
        return dao.getTransactionsByCategory(categoryId);
    }

    public LiveData<Double> getMonthlyExpense(long startMs, long endMs) {
        return dao.getMonthlyExpense(startMs, endMs);
    }

    public LiveData<Double> getMonthlyIncome(long startMs, long endMs) {
        return dao.getMonthlyIncome(startMs, endMs);
    }

    public LiveData<List<CategoryTotal>> getExpenseByCategory(long startMs, long endMs) {
        return dao.getExpenseByCategory(startMs, endMs);
    }

    public LiveData<List<CategoryTotal>> getIncomeByCategory(long startMs, long endMs) {
        return dao.getIncomeByCategory(startMs, endMs);
    }

    public LiveData<List<MonthlyTotal>> getMonthlyExpenseTrend() {
        return dao.getMonthlyExpenseTrend();
    }

    public LiveData<List<MonthlyTotal>> getMonthlyIncomeTrend() {
        return dao.getMonthlyIncomeTrend();
    }

    public LiveData<Integer> getTransactionCount() {
        return dao.getTransactionCount();
    }

    /** 同步方法：获取某月账单（用于导出） */
    public List<Transaction> getTransactionsByDateRangeSync(long startMs, long endMs) {
        return dao.getTransactionsByDateRangeSync(startMs, endMs);
    }

    public LiveData<List<Transaction>> searchByKeyword(String keyword) {
        return dao.searchByKeyword(keyword);
    }

    public LiveData<List<Transaction>> searchByCategoryAndDate(long categoryId, long startMs, long endMs) {
        return dao.searchByCategoryAndDate(categoryId, startMs, endMs);
    }

    public LiveData<List<Transaction>> searchByDateRange(long startMs, long endMs) {
        return dao.searchByDateRange(startMs, endMs);
    }
}
