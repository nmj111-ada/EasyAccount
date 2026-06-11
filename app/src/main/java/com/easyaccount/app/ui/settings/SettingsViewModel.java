package com.easyaccount.app.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.easyaccount.app.data.entity.Category;
import com.easyaccount.app.data.entity.Transaction;
import com.easyaccount.app.data.repository.CategoryRepository;
import com.easyaccount.app.data.repository.TransactionRepository;

import java.util.List;

public class SettingsViewModel extends AndroidViewModel {

    private final CategoryRepository catRepo;
    private final TransactionRepository txRepo;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        catRepo = new CategoryRepository(application);
        txRepo = new TransactionRepository(application);
    }

    public LiveData<List<Category>> getCategories() {
        return catRepo.getAllCategories();
    }

    public void insertCategory(Category category) {
        catRepo.insert(category);
    }

    public void updateCategory(Category category) {
        catRepo.update(category);
    }

    public void deleteCategory(Category category) {
        catRepo.delete(category);
    }

    public List<Transaction> exportAllTransactions(long startMs, long endMs) {
        return txRepo.getTransactionsByDateRangeSync(startMs, endMs);
    }

    public TransactionRepository getTransactionRepository() {
        return txRepo;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application app;
        public Factory(Application app) { this.app = app; }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SettingsViewModel(app);
        }
    }
}
