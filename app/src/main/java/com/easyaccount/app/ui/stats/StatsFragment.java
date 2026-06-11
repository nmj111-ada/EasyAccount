package com.easyaccount.app.ui.stats;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.easyaccount.app.R;
import com.easyaccount.app.data.entity.Category;
import com.easyaccount.app.data.entity.CategoryTotal;
import com.easyaccount.app.data.entity.MonthlyTotal;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsFragment extends Fragment {

    // ── Pie constants ──
    private static final float PIE_L = 30f, PIE_T = 10f, PIE_R = 30f, PIE_B = 10f;
    private static final float HOLE_R = 50f, TRANS_R = 54f;
    private static final float CENTER_SZ = 15f, MIN_OFF = 40f;
    private static final float LABEL_SZ = 12f, SLICE_GAP = 3f, MIN_PCT = 3f;
    private static final float LINE_W = 1f, LINE_L1 = 0.7f, LINE_L2 = 0.5f;
    private static final float LINE_OFF = 80f;
    private static final int PIE_ANIM = 1000, PIE_QUICK = 600;
    private static final float BAR_W = 0.4f;
    private static final int BAR_ANIM = 800;

    private PieChart pieChart;
    private BarChart barChart;
    private TextView tvMonthLabel;
    private int currentMonthOffset = 0;
    private boolean firstLoad = true;

    private static final int[] YELLOW_COLORS = {
            0xFFFFC107, 0xFFF9A825, 0xFFFFD54F, 0xFFFFA000,
            0xFFFF8F00, 0xFFFFCA28, 0xFFFFB300
    };

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c,
                                                   @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_stats, c, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        StatsViewModel vm = new ViewModelProvider(this,
                new StatsViewModel.Factory(requireActivity().getApplication())).get(StatsViewModel.class);

        pieChart = v.findViewById(R.id.pie_chart);
        barChart = v.findViewById(R.id.bar_chart);
        tvMonthLabel = v.findViewById(R.id.tv_month_label);
        tvMonthLabel.setText(vm.getCurrentMonthLabel());

        setupPie();
        setupBar();

        loadPie(vm);
        loadBar(vm);

        v.findViewById(R.id.btn_prev_month).setOnClickListener(x -> {
            currentMonthOffset--; vm.switchMonth(currentMonthOffset);
            tvMonthLabel.setText(vm.getCurrentMonthLabel());
            loadPie(vm);
        });
        v.findViewById(R.id.btn_next_month).setOnClickListener(x -> {
            currentMonthOffset++; vm.switchMonth(currentMonthOffset);
            tvMonthLabel.setText(vm.getCurrentMonthLabel());
            loadPie(vm);
        });
    }

    private void setupPie() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(PIE_L, PIE_T, PIE_R, PIE_B);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(HOLE_R);
        pieChart.setTransparentCircleRadius(TRANS_R);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setCenterText("¥");
        pieChart.setCenterTextSize(CENTER_SZ);
        pieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
        pieChart.setDrawEntryLabels(false);
        pieChart.setMinOffset(MIN_OFF);
        pieChart.setRotationEnabled(false);
        pieChart.getLegend().setEnabled(false);
    }

    private void setupBar() {
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.setScaleEnabled(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setGranularity(1f);
        barChart.getAxisLeft().setAxisMinimum(0f);
        Legend bl = barChart.getLegend();
        bl.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        bl.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        bl.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        bl.setDrawInside(true);
    }

    private void loadPie(StatsViewModel vm) {
        vm.getExpenseByCategory().observe(getViewLifecycleOwner(), totals -> {
            if (totals == null || totals.isEmpty()) {
                pieChart.setCenterText("暂无数据");
                pieChart.clear();
                return;
            }
            Map<Long, Category> catMap = vm.getCategoryMap();
            if (catMap.isEmpty()) {
                // 重新观察
                loadPie(vm);
                return;
            }

            // 按 parentCategory 聚合
            Map<String, Double> grouped = new HashMap<>();
            double all = 0;
            for (CategoryTotal ct : totals) {
                Category cat = catMap.get(ct.categoryId);
                String p = (cat != null && cat.getParentCategory() != null) ? cat.getParentCategory() : "其他";
                grouped.put(p, grouped.getOrDefault(p, 0.0) + ct.total);
                all += ct.total;
            }

            String[] order = {"餐饮","购物","居住","交通","教育","娱乐","其他"};
            List<PieEntry> entries = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();

            for (int i = 0; i < order.length; i++) {
                Double v = grouped.get(order[i]);
                if (v != null && v > 0) {
                    float percent = (float)(v / all * 100f);
                    if (percent < MIN_PCT) {
                        grouped.put("其他",
                                grouped.getOrDefault("其他", 0.0) + v);
                        continue;
                    }
                    entries.add(new PieEntry((float)(v / all * 100), order[i]));
                    colors.add(YELLOW_COLORS[i % YELLOW_COLORS.length]);
                }
            }
            if (entries.isEmpty()) {
                pieChart.setCenterText("暂无数据");
                pieChart.clear();
                return;
            }

            PieDataSet ds = new PieDataSet(entries, "");

            ds.setColors(colors);
            ds.setValueTextSize(LABEL_SZ);
            ds.setSliceSpace(SLICE_GAP);

// 外部标签显示（引线）
            ds.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            ds.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

// 引线样式优化（防重叠关键）
            ds.setValueLineColor(0xFF999999);
            ds.setValueLineWidth(LINE_W);
            ds.setValueLinePart1Length(LINE_L1);
            ds.setValueLinePart2Length(LINE_L2);
            ds.setValueLineVariableLength(true);
            ds.setValueLinePart1OffsetPercentage(LINE_OFF);

//  关键：控制“餐饮 25.50%”这种格式

            ds.setValueLineVariableLength(true);
            ds.setValueLinePart1OffsetPercentage(LINE_OFF);

            PieData data = new PieData(ds);

            data.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
                @Override
                public String getPieLabel(float value, PieEntry entry) {
                    return entry.getLabel() + " " + String.format("%.2f%%", value);
                }
            });
            
            pieChart.setCenterText("¥ " + (int)all);
            pieChart.setData(data);
            pieChart.post(() -> pieChart.animateY(firstLoad ? PIE_ANIM : PIE_QUICK, Easing.EaseInOutCubic));
            firstLoad = false;
            pieChart.invalidate();
        });
    }

    private void loadBar(StatsViewModel vm) {
        LiveData<List<MonthlyTotal>> ex = vm.getMonthlyExpenseTrend();
        LiveData<List<MonthlyTotal>> in = vm.getMonthlyIncomeTrend();
        ex.observe(getViewLifecycleOwner(), ed -> in.observe(getViewLifecycleOwner(), id -> {
            Calendar now = Calendar.getInstance();
            Map<Integer, float[]> m = new HashMap<>();
            if (ed != null) for (MonthlyTotal mt : ed) m.computeIfAbsent(mt.getMonthKey(), k -> new float[2])[0] = (float) mt.total;
            if (id != null) for (MonthlyTotal mt : id) m.computeIfAbsent(mt.getMonthKey(), k -> new float[2])[1] = (float) mt.total;

            List<BarEntry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            for (int mo = 1; mo <= 12; mo++) {
                int key = now.get(Calendar.YEAR) * 12 + mo;
                float[] v = m.getOrDefault(key, new float[]{0,0});
                labels.add(mo + "月");
                entries.add(new BarEntry(mo - 1, new float[]{v[0], v[1]}));
            }
            BarDataSet ds = new BarDataSet(entries, "");
            ds.setColors(new int[]{getResources().getColor(R.color.expense_color), getResources().getColor(R.color.income_color)});
            ds.setStackLabels(new String[]{"支出","收入"});
            ds.setDrawValues(false);
            BarData bd = new BarData(ds);
            bd.setBarWidth(BAR_W);
            barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
            barChart.setData(bd);
            barChart.post(() -> barChart.animateY(BAR_ANIM));
            barChart.invalidate();
        }));
    }
}
