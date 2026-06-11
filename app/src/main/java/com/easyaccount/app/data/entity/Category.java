package com.easyaccount.app.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * 账单分类实体
 */
@Entity(tableName = "categories")
public class Category {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /** 分类名称，如 "餐饮"、"交通" */
    private String name;

    /** drawable 资源 ID */
    private int iconRes;

    /** 颜色值 */
    private String color;

    /** 类型：0=支出分类，1=收入分类，2=通用 */
    private int type;

    /** 排序序号 */
    private int sortOrder;

    /** 一级分类（用于统计聚合） */
    private String parentCategory;

    public Category() {}

    @Ignore
    public Category(String name, int iconRes, String color, int type, String parentCategory) {
        this.name = name;
        this.iconRes = iconRes;
        this.color = color;
        this.type = type;
        this.parentCategory = parentCategory;
    }

    // ===== Getters & Setters =====

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getIconRes() { return iconRes; }
    public void setIconRes(int iconRes) { this.iconRes = iconRes; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public String getParentCategory() { return parentCategory; }
    public void setParentCategory(String parentCategory) { this.parentCategory = parentCategory; }
}
