package com.easyaccount.app.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * 标签实体
 */
@Entity(tableName = "tags")
public class Tag {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /** 标签名，如 "午餐"、"外卖" */
    private String name;

    /** 颜色值 */
    private String color;

    /** 所属分类ID（可选，方便按分类分组标签） */
    private long categoryId;

    public Tag() {}

    @Ignore
    public Tag(String name, String color, long categoryId) {
        this.name = name;
        this.color = color;
        this.categoryId = categoryId;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }
}
