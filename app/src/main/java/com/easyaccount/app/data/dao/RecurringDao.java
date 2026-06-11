package com.easyaccount.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.easyaccount.app.data.entity.RecurringRule;

import java.util.List;

@Dao
public interface RecurringDao {

    @Insert
    long insert(RecurringRule rule);

    @Update
    void update(RecurringRule rule);

    @Delete
    void delete(RecurringRule rule);

    @Query("SELECT * FROM recurring_rules WHERE enabled = 1 ORDER BY nextDateMs ASC")
    LiveData<List<RecurringRule>> getEnabledRules();

    @Query("SELECT * FROM recurring_rules ORDER BY nextDateMs ASC")
    LiveData<List<RecurringRule>> getAllRules();

    @Query("SELECT * FROM recurring_rules WHERE enabled = 1 AND nextDateMs <= :nowMs")
    List<RecurringRule> getDueRulesSync(long nowMs);

    @Query("SELECT * FROM recurring_rules")
    List<RecurringRule> getAllRulesSync();
}
