package com.easyaccount.app.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.easyaccount.app.data.entity.Budget;
import com.easyaccount.app.data.entity.Category;
import com.easyaccount.app.data.entity.Tag;
import com.easyaccount.app.data.entity.Transaction;
import com.easyaccount.app.data.repository.BudgetRepository;
import com.easyaccount.app.data.repository.CategoryRepository;
import com.easyaccount.app.data.repository.TagRepository;
import com.easyaccount.app.data.repository.TransactionRepository;
import com.easyaccount.app.data.repository.TransactionRepository.InsertCallback;

import java.util.Calendar;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final TransactionRepository txRepo;
    private final CategoryRepository catRepo;
    private final TagRepository tagRepo;
    private final BudgetRepository budgetRepo;

    private final MediatorLiveData<List<Transaction>> transactions = new MediatorLiveData<>();
    private final MediatorLiveData<Double> monthlyExpense = new MediatorLiveData<>();
    private final MediatorLiveData<Double> monthlyIncome = new MediatorLiveData<>();

    private LiveData<List<Transaction>> currentTxSource;
    private LiveData<Double> currentExpenseSource;
    private LiveData<Double> currentIncomeSource;

    /** 当前选中的月份起始毫秒 */
    private long currentMonthStartMs;

    public HomeViewModel(@NonNull Application application) {
        super(application);

        txRepo = new TransactionRepository(application);
        catRepo = new CategoryRepository(application);
        tagRepo = new TagRepository(application);
        budgetRepo = new BudgetRepository(application);

        // 初始加载全部账单 + 当月统计
        currentMonthStartMs = getMonthStartMs(0);
        long monthEndMs = getMonthEndMs(currentMonthStartMs);

        currentTxSource = txRepo.getAllTransactions();
        currentExpenseSource = txRepo.getMonthlyExpense(currentMonthStartMs, monthEndMs);
        currentIncomeSource = txRepo.getMonthlyIncome(currentMonthStartMs, monthEndMs);

        transactions.addSource(currentTxSource, transactions::setValue);
        monthlyExpense.addSource(currentExpenseSource, monthlyExpense::setValue);
        monthlyIncome.addSource(currentIncomeSource, monthlyIncome::setValue);
    }

    private void loadDataForMonth(long monthStartMs) {
        long monthEndMs = getMonthEndMs(monthStartMs);

        if (currentTxSource != null) transactions.removeSource(currentTxSource);
        if (currentExpenseSource != null) monthlyExpense.removeSource(currentExpenseSource);
        if (currentIncomeSource != null) monthlyIncome.removeSource(currentIncomeSource);

        currentTxSource = txRepo.getAllTransactions();
        currentExpenseSource = txRepo.getMonthlyExpense(monthStartMs, monthEndMs);
        currentIncomeSource = txRepo.getMonthlyIncome(monthStartMs, monthEndMs);

        transactions.addSource(currentTxSource, transactions::setValue);
        monthlyExpense.addSource(currentExpenseSource, monthlyExpense::setValue);
        monthlyIncome.addSource(currentIncomeSource, monthlyIncome::setValue);
    }

    public LiveData<List<Transaction>> getCurrentMonthTransactions() {
        return transactions;
    }

    public LiveData<Double> getMonthlyExpense() {
        return monthlyExpense;
    }

    public LiveData<Double> getMonthlyIncome() {
        return monthlyIncome;
    }

    public LiveData<List<Category>> getAllCategories() {
        return catRepo.getAllCategories();
    }

    public LiveData<List<Category>> getCategoriesByType(int type) {
        return catRepo.getCategoriesByType(type);
    }

    public LiveData<Category> getCategoryById(long id) {
        return catRepo.getCategoryById(id);
    }

    public LiveData<List<Tag>> getTagsByCategory(long categoryId) {
        return tagRepo.getTagsByCategory(categoryId);
    }

    public List<Tag> getAllTagsSync() {
        return tagRepo.getAllTagsSync();
    }

    public void addTagToTransaction(long txId, long tagId) {
        tagRepo.addTagToTransaction(txId, tagId);
    }

    public void setTransactionTags(long txId, List<Long> tagIds) {
        tagRepo.setTransactionTags(txId, tagIds);
    }

    public void insert(Transaction transaction) {
        txRepo.insert(transaction);
    }

    /** 插入并回调返回ID */
    public void insert(Transaction transaction, TransactionRepository.InsertCallback callback) {
        txRepo.insert(transaction, callback);
    }

    public void update(Transaction transaction) {
        txRepo.update(transaction);
    }

    public void delete(Transaction transaction) {
        txRepo.delete(transaction);
    }

    /** 刷新月度数据（添加/删除后调用） */
    public void refreshMonthlyData() {
        loadDataForMonth(currentMonthStartMs);
    }

    /** 获取总预算 */
    public LiveData<Budget> getTotalBudget() {
        return budgetRepo.getTotalBudget();
    }

    /** 设置/更新总预算 */
    public void saveTotalBudget(double limit) {
        executor.execute(() -> {
            Budget existing = budgetRepo.getTotalBudgetSync();
            if (existing != null) {
                existing.setMonthlyLimit(limit);
                budgetRepo.update(existing);
            } else {
                Budget b = new Budget();
                b.setCategoryId(null);
                b.setMonthlyLimit(limit);
                budgetRepo.insert(b);
            }
        });
    }

    private final java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();

    /** 切换月份 */
    public void switchMonth(int offset) {
        currentMonthStartMs = getMonthStartMs(offset);
        loadDataForMonth(currentMonthStartMs);
    }

    private long getMonthStartMs(int monthOffset) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, monthOffset);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getMonthEndMs(long monthStartMs) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(monthStartMs);
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.MILLISECOND, -1);
        return cal.getTimeInMillis();
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application app;
        public Factory(Application app) { this.app = app; }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new HomeViewModel(app);
        }
    }
}
