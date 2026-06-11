package com.easyaccount.app.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.easyaccount.app.data.AppDatabase;
import com.easyaccount.app.data.entity.RecurringRule;
import com.easyaccount.app.data.entity.Transaction;

import java.util.Calendar;
import java.util.List;

/**
 * 周期记账 Worker
 * 每天检查一次，找到到期的规则并自动生成账单
 */
public class RecurringWorker extends Worker {

    public RecurringWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            long nowMs = System.currentTimeMillis();
            List<RecurringRule> dueRules = AppDatabase.getInstance(getApplicationContext())
                    .recurringDao().getDueRulesSync(nowMs);

            for (RecurringRule rule : dueRules) {
                // 生成账单
                Transaction tx = new Transaction();
                tx.setAmount(rule.getAmount());
                tx.setType(rule.getType());
                tx.setCategoryId(rule.getCategoryId());
                tx.setNote(rule.getNote());
                tx.setDateMs(rule.getNextDateMs());
                AppDatabase.getInstance(getApplicationContext())
                        .transactionDao().insert(tx);

                // 计算下次日期
                rule.setNextDateMs(computeNextDate(rule));
                AppDatabase.getInstance(getApplicationContext())
                        .recurringDao().update(rule);
            }

            Log.d("RecurringWorker", "Generated " + dueRules.size() + " recurring transactions");
            return Result.success();
        } catch (Exception e) {
            Log.e("RecurringWorker", "Error processing recurring rules", e);
            return Result.retry();
        }
    }

    private long computeNextDate(RecurringRule rule) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(rule.getNextDateMs());
        switch (rule.getPeriodType()) {
            case 0: cal.add(Calendar.DAY_OF_MONTH, 1); break;
            case 1: cal.add(Calendar.WEEK_OF_YEAR, 1); break;
            case 3: cal.add(Calendar.YEAR, 1); break;
            case 2: default: cal.add(Calendar.MONTH, 1); break;
        }
        return cal.getTimeInMillis();
    }
}
