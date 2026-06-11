package com.easyaccount.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.easyaccount.app.data.entity.Budget;

import java.util.List;

@Dao
public interface BudgetDao {

    @Insert
    long insert(Budget budget);

    @Update
    void update(Budget budget);

    @Delete
    void delete(Budget budget);

    @Query("SELECT * FROM budgets WHERE categoryId IS NULL LIMIT 1")
    LiveData<Budget> getTotalBudget();

    @Query("SELECT * FROM budgets WHERE categoryId IS NULL LIMIT 1")
    Budget getTotalBudgetSync();

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId LIMIT 1")
    LiveData<Budget> getCategoryBudget(long categoryId);

    @Query("SELECT * FROM budgets")
    LiveData<List<Budget>> getAllBudgets();

    @Query("SELECT * FROM budgets")
    List<Budget> getAllBudgetsSync();
}
