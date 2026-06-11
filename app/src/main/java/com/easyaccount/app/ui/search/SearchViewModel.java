package com.easyaccount.app.ui.search;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.easyaccount.app.data.entity.Category;
import com.easyaccount.app.data.entity.Transaction;
import com.easyaccount.app.data.repository.CategoryRepository;
import com.easyaccount.app.data.repository.TransactionRepository;

import java.util.List;

public class SearchViewModel extends AndroidViewModel {

    private final TransactionRepository txRepo;
    private final CategoryRepository catRepo;

    public SearchViewModel(@NonNull Application application) {
        super(application);
        txRepo = new TransactionRepository(application);
        catRepo = new CategoryRepository(application);
    }

    public LiveData<List<Transaction>> searchByKeyword(String keyword) {
        return txRepo.searchByKeyword(keyword);
    }

    public LiveData<List<Transaction>> searchByDateRange(long start, long end) {
        return txRepo.searchByDateRange(start, end);
    }

    public LiveData<List<Transaction>> searchByCategoryAndDate(long categoryId, long start, long end) {
        return txRepo.searchByCategoryAndDate(categoryId, start, end);
    }

    public LiveData<List<Category>> getAllCategories() {
        return catRepo.getAllCategories();
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application app;
        public Factory(Application app) { this.app = app; }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SearchViewModel(app);
        }
    }
}
