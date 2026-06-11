package com.easyaccount.app.data.entity;

/**
 * 月度统计查询结果
 */
public class MonthlyTotal {
    public int monthKey; // year*12 + month，如 2025*12+5=24305
    public double total;

    public int getMonthKey() { return monthKey; }
    public void setMonthKey(int monthKey) { this.monthKey = monthKey; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}
