package com.easyaccount.app.ui.stats;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.easyaccount.app.data.entity.Category;
import com.easyaccount.app.data.entity.CategoryTotal;
import com.easyaccount.app.data.entity.MonthlyTotal;
import com.easyaccount.app.data.repository.CategoryRepository;
import com.easyaccount.app.data.repository.TransactionRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatsViewModel extends AndroidViewModel {

    private final TransactionRepository txRepo;
    private final CategoryRepository catRepo;
    private long currentMonthStartMs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile Map<Long, Category> categoryMap = new HashMap<>();

    public StatsViewModel(@NonNull Application application) {
        super(application);
        txRepo = new TransactionRepository(application);
        catRepo = new CategoryRepository(application);
        currentMonthStartMs = getMonthStartMs(0);

        // 异步预加载所有分类
        executor.execute(() -> {
            Map<Long, Category> map = new HashMap<>();
            List<Category> categories = catRepo.getAllCategoriesSync();
            for (Category cat : categories) {
                map.put(cat.getId(), cat);
            }
            categoryMap = map;
        });
    }

    public LiveData<List<CategoryTotal>> getExpenseByCategory() {
        long endMs = getMonthEndMs(currentMonthStartMs);
        return txRepo.getExpenseByCategory(currentMonthStartMs, endMs);
    }

    /** 按一级分类聚合的支出数据 */
    public LiveData<List<CategoryTotal>> getExpenseByParentCategory() {
        long endMs = getMonthEndMs(currentMonthStartMs);
        androidx.lifecycle.MediatorLiveData<List<CategoryTotal>> result = new androidx.lifecycle.MediatorLiveData<>();
        result.addSource(txRepo.getExpenseByCategory(currentMonthStartMs, endMs), categoryTotals -> {
            if (categoryTotals == null) { result.setValue(new ArrayList<>()); return; }
            Map<String, Double> grouped = new HashMap<>();
            for (CategoryTotal ct : categoryTotals) {
                Category cat = categoryMap.get(ct.categoryId);
                String parent = cat != null && cat.getParentCategory() != null
                        ? cat.getParentCategory() : "其他";
                grouped.put(parent, grouped.getOrDefault(parent, 0.0) + ct.total);
            }
            List<CategoryTotal> list = new ArrayList<>();
            for (Map.Entry<String, Double> e : grouped.entrySet()) {
                CategoryTotal ct = new CategoryTotal();
                ct.total = e.getValue();
                // 借用 categoryId 存储 hash 用于区分
                ct.categoryId = e.getKey().hashCode();
                list.add(ct);
            }
            list.sort((a, b) -> Double.compare(b.total, a.total));
            result.setValue(list);
        });
        return result;
    }

    public LiveData<List<CategoryTotal>> getIncomeByCategory() {
        long endMs = getMonthEndMs(currentMonthStartMs);
        return txRepo.getIncomeByCategory(currentMonthStartMs, endMs);
    }

    public LiveData<List<MonthlyTotal>> getMonthlyExpenseTrend() {
        return txRepo.getMonthlyExpenseTrend();
    }

    public LiveData<List<MonthlyTotal>> getMonthlyIncomeTrend() {
        return txRepo.getMonthlyIncomeTrend();
    }

    public LiveData<Category> getCategoryById(long id) {
        return catRepo.getCategoryById(id);
    }

    /** 获取分类映射（已异步预加载） */
    public Map<Long, Category> getCategoryMap() {
        return categoryMap;
    }

    public String getCurrentMonthLabel() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentMonthStartMs);
        return cal.get(Calendar.YEAR) + "年" + (cal.get(Calendar.MONTH) + 1) + "月";
    }

    /** 切换月份 */
    public void switchMonth(int offset) {
        currentMonthStartMs = getMonthStartMs(offset);
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
            return (T) new StatsViewModel(app);
        }
    }
}
