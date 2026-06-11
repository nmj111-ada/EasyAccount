package com.easyaccount.app.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.easyaccount.app.data.dao.BudgetDao;
import com.easyaccount.app.data.dao.CategoryDao;
import com.easyaccount.app.data.dao.RecurringDao;
import com.easyaccount.app.data.dao.TagDao;
import com.easyaccount.app.data.dao.TransactionDao;
import com.easyaccount.app.data.dao.TransactionTagDao;
import com.easyaccount.app.data.entity.*;

@Database(entities = {
        Transaction.class, Category.class, Tag.class, TransactionTag.class,
        Budget.class, RecurringRule.class
}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TransactionDao transactionDao();
    public abstract CategoryDao categoryDao();
    public abstract TagDao tagDao();
    public abstract TransactionTagDao transactionTagDao();
    public abstract BudgetDao budgetDao();
    public abstract RecurringDao recurringDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class, "easy_account.db")
                            .allowMainThreadQueries()
                            .addCallback(new SeedDatabaseCallback())
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS tags (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT, color TEXT, categoryId INTEGER NOT NULL DEFAULT 0)");
            db.execSQL("CREATE TABLE IF NOT EXISTS transaction_tags (transactionId INTEGER NOT NULL, tagId INTEGER NOT NULL, PRIMARY KEY(transactionId, tagId), FOREIGN KEY(transactionId) REFERENCES transactions(id) ON DELETE CASCADE, FOREIGN KEY(tagId) REFERENCES tags(id) ON DELETE CASCADE)");
            db.execSQL("CREATE TABLE IF NOT EXISTS budgets (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, categoryId INTEGER, monthlyLimit REAL NOT NULL DEFAULT 0, notifyOnExceed INTEGER NOT NULL DEFAULT 1)");
            db.execSQL("CREATE TABLE IF NOT EXISTS recurring_rules (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, categoryId INTEGER NOT NULL, amount REAL NOT NULL, type INTEGER NOT NULL DEFAULT 0, note TEXT, periodType INTEGER NOT NULL DEFAULT 2, nextDateMs INTEGER NOT NULL, enabled INTEGER NOT NULL DEFAULT 1)");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE categories ADD COLUMN parentCategory TEXT");
            db.execSQL("ALTER TABLE categories ADD COLUMN iconRes INTEGER NOT NULL DEFAULT 0");
            db.execSQL("DELETE FROM categories");
            seedCategories(db);
        }
    };

    private static class SeedDatabaseCallback extends Callback {
        @Override public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            seedCategories(db);
        }
    }

    // ====== 种子数据：7大类 + 42二级分类 ======

    private static void seedCategories(SupportSQLiteDatabase db) {
        // 图标映射：R.drawable.ic_cat_xxx 的实际 int 值在编译时确定
        // 这里用占位值，首次运行 onCreate 时通过数据库预填
        int FOOD = 0, SHOP = 1, HOUSE = 2, TRANS = 3, EDU = 4, FUN = 5, OTHER = 6;

        // 支出-餐饮 (parent=餐饮)
        insert(db, "餐饮", FOOD, "#FF7043", 0, 1, "餐饮");
        insert(db, "外卖", FOOD, "#FF7043", 0, 2, "餐饮");
        insert(db, "饮料", FOOD, "#FF7043", 0, 3, "餐饮");
        insert(db, "零食", FOOD, "#FF7043", 0, 4, "餐饮");
        insert(db, "水果", FOOD, "#FF7043", 0, 5, "餐饮");
        insert(db, "烟酒", FOOD, "#FF7043", 0, 6, "餐饮");

        // 支出-购物 (parent=购物)
        insert(db, "购物", SHOP, "#AB47BC", 0, 7, "购物");
        insert(db, "数码", SHOP, "#AB47BC", 0, 8, "购物");
        insert(db, "服装", SHOP, "#AB47BC", 0, 9, "购物");
        insert(db, "日用品", SHOP, "#AB47BC", 0, 10, "购物");
        insert(db, "宠物", SHOP, "#AB47BC", 0, 11, "购物");
        insert(db, "母婴", SHOP, "#AB47BC", 0, 12, "购物");
        insert(db, "美容", SHOP, "#AB47BC", 0, 13, "购物");
        insert(db, "送礼", SHOP, "#AB47BC", 0, 14, "购物");

        // 支出-居住
        insert(db, "居家", HOUSE, "#5C6BC0", 0, 15, "居住");
        insert(db, "水电", HOUSE, "#5C6BC0", 0, 16, "居住");
        insert(db, "物业", HOUSE, "#5C6BC0", 0, 17, "居住");
        insert(db, "通讯", HOUSE, "#5C6BC0", 0, 18, "居住");
        insert(db, "住房", HOUSE, "#5C6BC0", 0, 19, "居住");
        insert(db, "装修", HOUSE, "#5C6BC0", 0, 20, "居住");
        insert(db, "社保", HOUSE, "#5C6BC0", 0, 21, "居住");
        insert(db, "纳税", HOUSE, "#5C6BC0", 0, 22, "居住");

        // 支出-交通
        insert(db, "交通", TRANS, "#42A5F5", 0, 23, "交通");
        insert(db, "汽车", TRANS, "#42A5F5", 0, 24, "交通");
        insert(db, "打车", TRANS, "#42A5F5", 0, 25, "交通");
        insert(db, "地铁", TRANS, "#42A5F5", 0, 26, "交通");
        insert(db, "停车", TRANS, "#42A5F5", 0, 27, "交通");
        insert(db, "旅行", TRANS, "#42A5F5", 0, 28, "交通");

        // 支出-教育
        insert(db, "教育", EDU, "#66BB6A", 0, 29, "教育");
        insert(db, "学习", EDU, "#66BB6A", 0, 30, "教育");
        insert(db, "书", EDU, "#66BB6A", 0, 31, "教育");

        // 支出-娱乐
        insert(db, "娱乐", FUN, "#26A69A", 0, 32, "娱乐");
        insert(db, "游戏", FUN, "#26A69A", 0, 33, "娱乐");
        insert(db, "运动", FUN, "#26A69A", 0, 34, "娱乐");
        insert(db, "社交", FUN, "#26A69A", 0, 35, "娱乐");
        insert(db, "爱人", FUN, "#26A69A", 0, 36, "娱乐");

        // 支出-其他
        insert(db, "家庭", OTHER, "#78909C", 0, 37, "其他");
        insert(db, "医疗", OTHER, "#78909C", 0, 38, "其他");
        insert(db, "维修", OTHER, "#78909C", 0, 39, "其他");
        insert(db, "快递", OTHER, "#78909C", 0, 40, "其他");
        insert(db, "工作", OTHER, "#78909C", 0, 41, "其他");
        insert(db, "捐赠", OTHER, "#78909C", 0, 42, "其他");
        insert(db, "红包", OTHER, "#78909C", 0, 43, "其他");
        insert(db, "彩票", OTHER, "#78909C", 0, 44, "其他");
        insert(db, "股票", OTHER, "#78909C", 0, 45, "其他");

        // 收入分类
        insert(db, "工资", SHOP, "#FFA726", 1, 1, "收入");
        insert(db, "兼职", SHOP, "#FFA726", 1, 2, "收入");
        insert(db, "红包", SHOP, "#FFA726", 1, 3, "收入");
        insert(db, "投资收益", SHOP, "#FFA726", 1, 4, "收入");
        insert(db, "其他收入", SHOP, "#FFA726", 1, 5, "收入");
    }

    private static void insert(SupportSQLiteDatabase db, String name, int iconRes, String color, int type, int order, String parent) {
        db.execSQL("INSERT INTO categories (name, iconRes, color, type, sortOrder, parentCategory) VALUES (?,?,?,?,?,?)",
                new Object[]{name, iconRes, color, type, order, parent});
    }
}
