package com.easyaccount.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.easyaccount.app.data.entity.Category;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert
    long insert(Category category);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    /** 获取所有分类 */
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    LiveData<List<Category>> getAllCategories();

    /** 按类型获取分类（0=支出，1=收入） */
    @Query("SELECT * FROM categories WHERE type = :type OR type = 2 ORDER BY sortOrder ASC")
    LiveData<List<Category>> getCategoriesByType(int type);

    /** 根据ID获取分类 */
    @Query("SELECT * FROM categories WHERE id = :id")
    LiveData<Category> getCategoryById(long id);

    /** 检查分类名是否已存在 */
    @Query("SELECT COUNT(*) FROM categories WHERE name = :name")
    int countByName(String name);

    /** 同步获取所有分类 */
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    List<Category> getAllCategoriesSync();

    /** 获取默认支出分类 */
    @Query("SELECT * FROM categories WHERE type = 0 ORDER BY sortOrder ASC")
    LiveData<List<Category>> getExpenseCategories();

    /** 获取默认收入分类 */
    @Query("SELECT * FROM categories WHERE type = 1 ORDER BY sortOrder ASC")
    LiveData<List<Category>> getIncomeCategories();
}
