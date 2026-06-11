package com.easyaccount.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.easyaccount.app.data.entity.Tag;

import java.util.List;

@Dao
public interface TagDao {

    @Insert
    long insert(Tag tag);

    @Update
    void update(Tag tag);

    @Delete
    void delete(Tag tag);

    @Query("SELECT * FROM tags ORDER BY name ASC")
    LiveData<List<Tag>> getAllTags();

    @Query("SELECT * FROM tags WHERE categoryId = :categoryId ORDER BY name ASC")
    LiveData<List<Tag>> getTagsByCategory(long categoryId);

    @Query("SELECT * FROM tags ORDER BY name ASC")
    List<Tag> getAllTagsSync();

    @Query("SELECT DISTINCT t.* FROM tags t INNER JOIN transaction_tags tt ON t.id = tt.tagId WHERE tt.transactionId = :txId")
    LiveData<List<Tag>> getTagsForTransaction(long txId);
}
