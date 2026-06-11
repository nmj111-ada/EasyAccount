package com.easyaccount.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.easyaccount.app.data.entity.Transaction;
import com.easyaccount.app.data.entity.CategoryTotal;
import com.easyaccount.app.data.entity.MonthlyTotal;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    /** 获取所有账单，按日期降序 */
    @Query("SELECT * FROM transactions ORDER BY dateMs DESC")
    LiveData<List<Transaction>> getAllTransactions();

    /** 按日期范围查询 */
    @Query("SELECT * FROM transactions WHERE dateMs >= :startMs AND dateMs <= :endMs ORDER BY dateMs DESC")
    LiveData<List<Transaction>> getTransactionsByDateRange(long startMs, long endMs);

    /** 按分类查询 */
    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY dateMs DESC")
    LiveData<List<Transaction>> getTransactionsByCategory(long categoryId);

    /** 获取某月的所有账单（同步，用于导出） */
    @Query("SELECT * FROM transactions WHERE dateMs >= :startMs AND dateMs <= :endMs ORDER BY dateMs DESC")
    List<Transaction> getTransactionsByDateRangeSync(long startMs, long endMs);

    @Query("SELECT * FROM transactions")
    List<Transaction> getAllTransactionsSync();

    @Query("SELECT COUNT(*) FROM transactions")
    long getTransactionCountSync();

    /** 获取某月的总支出 */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 0 AND dateMs >= :startMs AND dateMs <= :endMs")
    LiveData<Double> getMonthlyExpense(long startMs, long endMs);

    /** 获取某月的总收入 */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 1 AND dateMs >= :startMs AND dateMs <= :endMs")
    LiveData<Double> getMonthlyIncome(long startMs, long endMs);

    /** 按分类统计某月的支出 */
    @Query("SELECT categoryId, SUM(amount) as total FROM transactions WHERE type = 0 AND dateMs >= :startMs AND dateMs <= :endMs GROUP BY categoryId ORDER BY total DESC")
    LiveData<List<CategoryTotal>> getExpenseByCategory(long startMs, long endMs);

    /** 按分类统计某月的收入 */
    @Query("SELECT categoryId, SUM(amount) as total FROM transactions WHERE type = 1 AND dateMs >= :startMs AND dateMs <= :endMs GROUP BY categoryId ORDER BY total DESC")
    LiveData<List<CategoryTotal>> getIncomeByCategory(long startMs, long endMs);

    /** 总记录数 */
    @Query("SELECT COUNT(*) FROM transactions")
    LiveData<Integer> getTransactionCount();

    /** 月度支出趋势（按月分组，monthKey = year*12+month） */
    @Query("SELECT (strftime('%Y', dateMs/1000, 'unixepoch') * 12 + strftime('%m', dateMs/1000, 'unixepoch')) as monthKey, SUM(amount) as total FROM transactions WHERE type = 0 GROUP BY monthKey ORDER BY monthKey ASC")
    LiveData<List<MonthlyTotal>> getMonthlyExpenseTrend();

    /** 月度收入趋势 */
    @Query("SELECT (strftime('%Y', dateMs/1000, 'unixepoch') * 12 + strftime('%m', dateMs/1000, 'unixepoch')) as monthKey, SUM(amount) as total FROM transactions WHERE type = 1 GROUP BY monthKey ORDER BY monthKey ASC")
    LiveData<List<MonthlyTotal>> getMonthlyIncomeTrend();

    /** 按关键字搜索（备注或金额） */
    @Query("SELECT * FROM transactions WHERE note LIKE '%' || :keyword || '%' OR CAST(amount AS TEXT) LIKE '%' || :keyword || '%' ORDER BY dateMs DESC")
    LiveData<List<Transaction>> searchByKeyword(String keyword);

    /** 按分类+日期范围搜索 */
    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId AND dateMs >= :startMs AND dateMs <= :endMs ORDER BY dateMs DESC")
    LiveData<List<Transaction>> searchByCategoryAndDate(long categoryId, long startMs, long endMs);

    /** 按日期范围搜索 */
    @Query("SELECT * FROM transactions WHERE dateMs >= :startMs AND dateMs <= :endMs ORDER BY dateMs DESC")
    LiveData<List<Transaction>> searchByDateRange(long startMs, long endMs);
}
