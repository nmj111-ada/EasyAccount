package com.easyaccount.app;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.easyaccount.app.data.AppDatabase;
import com.easyaccount.app.sync.RecurringWorker;

import java.util.concurrent.TimeUnit;

/**
 * Application 类，用于全局初始化
 */
public class EasyAccountApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 读取深色模式设置并应用
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
        // 预初始化数据库（可选）
        AppDatabase.getInstance(this);

        // 启动周期记账 Worker（每天检查一次）
        PeriodicWorkRequest recurringWork = new PeriodicWorkRequest.Builder(
                RecurringWorker.class, 24, TimeUnit.HOURS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "recurring_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                recurringWork
        );
    }
}
