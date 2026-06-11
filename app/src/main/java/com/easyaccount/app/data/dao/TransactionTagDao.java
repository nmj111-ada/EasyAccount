package com.easyaccount.app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.easyaccount.app.data.entity.TransactionTag;

import java.util.List;

@Dao
public interface TransactionTagDao {

    @Insert
    void insert(TransactionTag transactionTag);

    @Insert
    void insertAll(List<TransactionTag> transactionTags);

    @Query("DELETE FROM transaction_tags WHERE transactionId = :txId")
    void deleteByTransaction(long txId);

    @Query("SELECT * FROM transaction_tags WHERE tagId = :tagId")
    List<TransactionTag> getByTag(long tagId);
}
