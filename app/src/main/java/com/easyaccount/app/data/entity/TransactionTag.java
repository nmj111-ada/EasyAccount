package com.easyaccount.app.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

/**
 * 账单-标签关联表（多对多）
 */
@Entity(tableName = "transaction_tags",
        primaryKeys = {"transactionId", "tagId"},
        foreignKeys = {
                @ForeignKey(entity = Transaction.class, parentColumns = "id",
                        childColumns = "transactionId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Tag.class, parentColumns = "id",
                        childColumns = "tagId", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("transactionId"), @Index("tagId")})
public class TransactionTag {

    private long transactionId;
    private long tagId;

    public TransactionTag() {}

    @Ignore
    public TransactionTag(long transactionId, long tagId) {
        this.transactionId = transactionId;
        this.tagId = tagId;
    }

    public long getTransactionId() { return transactionId; }
    public void setTransactionId(long transactionId) { this.transactionId = transactionId; }

    public long getTagId() { return tagId; }
    public void setTagId(long tagId) { this.tagId = tagId; }
}
