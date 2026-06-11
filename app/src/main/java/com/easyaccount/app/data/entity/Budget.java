package com.easyaccount.app.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 预算实体
 */
@Entity(tableName = "budgets",
        foreignKeys = @ForeignKey(entity = Category.class, parentColumns = "id",
                childColumns = "categoryId", onDelete = ForeignKey.CASCADE),
        indices = @Index("categoryId"))
public class Budget {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /** 分类ID，null 表示总预算 */
    private Long categoryId;

    /** 月度限额 */
    private double monthlyLimit;

    /** 是否启用超支提醒 */
    private boolean notifyOnExceed;

    public Budget() {
        this.notifyOnExceed = true;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public double getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(double monthlyLimit) { this.monthlyLimit = monthlyLimit; }

    public boolean isNotifyOnExceed() { return notifyOnExceed; }
    public void setNotifyOnExceed(boolean notifyOnExceed) { this.notifyOnExceed = notifyOnExceed; }
}
