package com.easyaccount.app.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.easyaccount.app.data.AppDatabase;
import com.easyaccount.app.data.dao.RecurringDao;
import com.easyaccount.app.data.entity.RecurringRule;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecurringRepository {

    private final RecurringDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public RecurringRepository(Context context) {
        dao = AppDatabase.getInstance(context).recurringDao();
    }

    public void insert(RecurringRule rule) {
        executor.execute(() -> dao.insert(rule));
    }

    public void update(RecurringRule rule) {
        executor.execute(() -> dao.update(rule));
    }

    public void delete(RecurringRule rule) {
        executor.execute(() -> dao.delete(rule));
    }

    public LiveData<List<RecurringRule>> getAllRules() {
        return dao.getAllRules();
    }

    public LiveData<List<RecurringRule>> getEnabledRules() {
        return dao.getEnabledRules();
    }

    public List<RecurringRule> getDueRulesSync(long nowMs) {
        return dao.getDueRulesSync(nowMs);
    }
}
