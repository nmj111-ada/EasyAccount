package com.easyaccount.app.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * 账单记录实体
 */
@Entity(tableName = "transactions",
        foreignKeys = @ForeignKey(
                entity = Category.class,
                parentColumns = "id",
                childColumns = "categoryId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index("categoryId"))
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /** 金额，正数为收入，负数为支出（用单独的 type 字段更好） */
    private double amount;

    /** 类型：0=支出，1=收入 */
    private int type;

    /** 分类ID */
    private long categoryId;

    /** 备注 */
    private String note;

    /** 日期（毫秒时间戳） */
    private long dateMs;

    /** 创建时间 */
    private long createdAt;

    public Transaction() {
        this.createdAt = System.currentTimeMillis();
        this.dateMs = System.currentTimeMillis();
    }

    // ===== Getters & Setters =====

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public long getDateMs() { return dateMs; }
    public void setDateMs(long dateMs) { this.dateMs = dateMs; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
