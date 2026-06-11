package com.easyaccount.app.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.easyaccount.app.data.AppDatabase;
import com.easyaccount.app.data.dao.TagDao;
import com.easyaccount.app.data.dao.TransactionTagDao;
import com.easyaccount.app.data.entity.Tag;
import com.easyaccount.app.data.entity.TransactionTag;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TagRepository {

    private final TagDao tagDao;
    private final TransactionTagDao ttDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public TagRepository(Context context) {
        tagDao = AppDatabase.getInstance(context).tagDao();
        ttDao = AppDatabase.getInstance(context).transactionTagDao();
    }

    public void insert(Tag tag) {
        executor.execute(() -> tagDao.insert(tag));
    }

    public void update(Tag tag) {
        executor.execute(() -> tagDao.update(tag));
    }

    public void delete(Tag tag) {
        executor.execute(() -> tagDao.delete(tag));
    }

    public LiveData<List<Tag>> getAllTags() {
        return tagDao.getAllTags();
    }

    public LiveData<List<Tag>> getTagsByCategory(long categoryId) {
        return tagDao.getTagsByCategory(categoryId);
    }

    public List<Tag> getAllTagsSync() {
        return tagDao.getAllTagsSync();
    }

    public void addTagToTransaction(long txId, long tagId) {
        executor.execute(() -> ttDao.insert(new TransactionTag(txId, tagId)));
    }

    public void setTransactionTags(long txId, List<Long> tagIds) {
        executor.execute(() -> {
            ttDao.deleteByTransaction(txId);
            for (Long tagId : tagIds) {
                ttDao.insert(new TransactionTag(txId, tagId));
            }
        });
    }
}
