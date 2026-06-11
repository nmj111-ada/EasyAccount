package com.easyaccount.app.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.easyaccount.app.data.AppDatabase;
import com.easyaccount.app.data.dao.CategoryDao;
import com.easyaccount.app.data.entity.Category;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryRepository {

    private final CategoryDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CategoryRepository(Context context) {
        dao = AppDatabase.getInstance(context).categoryDao();
    }

    public void insert(Category category) {
        executor.execute(() -> dao.insert(category));
    }

    public void update(Category category) {
        executor.execute(() -> dao.update(category));
    }

    public void delete(Category category) {
        executor.execute(() -> dao.delete(category));
    }

    public LiveData<List<Category>> getAllCategories() {
        return dao.getAllCategories();
    }

    public LiveData<List<Category>> getExpenseCategories() {
        return dao.getExpenseCategories();
    }

    public LiveData<List<Category>> getIncomeCategories() {
        return dao.getIncomeCategories();
    }

    public LiveData<Category> getCategoryById(long id) {
        return dao.getCategoryById(id);
    }

    public LiveData<List<Category>> getCategoriesByType(int type) {
        return dao.getCategoriesByType(type);
    }

    public boolean categoryExists(String name) {
        return dao.countByName(name) > 0;
    }

    public List<Category> getAllCategoriesSync() {
        return dao.getAllCategoriesSync();
    }
}
