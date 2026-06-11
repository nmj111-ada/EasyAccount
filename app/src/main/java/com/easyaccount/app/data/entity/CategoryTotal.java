package com.easyaccount.app.data.entity;

import androidx.room.ColumnInfo;

/**
 * 分类统计查询结果
 */
public class CategoryTotal {
    public long categoryId;

    public double total;

    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}
