package com.easyaccount.app.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 周期记账规则
 */
@Entity(tableName = "recurring_rules",
        foreignKeys = @ForeignKey(entity = Category.class, parentColumns = "id",
                childColumns = "categoryId", onDelete = ForeignKey.CASCADE),
        indices = @Index("categoryId"))
public class RecurringRule {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /** 分类ID */
    private long categoryId;

    /** 金额 */
    private double amount;

    /** 类型：0=支出 1=收入 */
    private int type;

    /** 备注 */
    private String note;

    /** 周期类型：0=每日 1=每周 2=每月 3=每年 */
    private int periodType;

    /** 下次生成日期（毫秒时间戳） */
    private long nextDateMs;

    /** 是否启用 */
    private boolean enabled;

    public RecurringRule() {
        this.enabled = true;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public int getPeriodType() { return periodType; }
    public void setPeriodType(int periodType) { this.periodType = periodType; }

    public long getNextDateMs() { return nextDateMs; }
    public void setNextDateMs(long nextDateMs) { this.nextDateMs = nextDateMs; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
