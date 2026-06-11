package com.easyaccount.app.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.easyaccount.app.R;
import com.easyaccount.app.data.entity.Budget;
import com.easyaccount.app.sync.SyncService;
import com.easyaccount.app.data.entity.Category;
import com.easyaccount.app.data.entity.RecurringRule;
import com.easyaccount.app.data.repository.BudgetRepository;
import com.easyaccount.app.data.repository.CategoryRepository;
import com.easyaccount.app.data.repository.RecurringRepository;
import com.easyaccount.app.data.repository.TransactionRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 「我的」页面 — 设置、预算、周期记账、分类管理、导出
 */
public class ProfileFragment extends Fragment {

    private Switch darkModeSwitch;
    private TextView tvVersion;
    private BudgetRepository budgetRepo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        darkModeSwitch = root.findViewById(R.id.switch_dark_mode);
        tvVersion = root.findViewById(R.id.tv_version);
        budgetRepo = new BudgetRepository(requireActivity().getApplication());

        // 版本号
        try {
            String version = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            tvVersion.setText("v" + version);
        } catch (Exception e) {
            tvVersion.setText("v1.0");
        }

        // 深色模式
        SharedPreferences prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        darkModeSwitch.setChecked(isDark);
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    isChecked ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                            : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            );
            requireActivity().recreate();
        });

        // 显示当前预算值
        budgetRepo.getTotalBudget().observe(getViewLifecycleOwner(), budget -> {
            TextView tvBudgetHint = root.findViewById(R.id.tv_budget_hint);
            if (budget != null && tvBudgetHint != null) {
                tvBudgetHint.setText(String.format("%.0f", budget.getMonthlyLimit()));
            }
        });

        // 显示周期规则数量
        new RecurringRepository(requireActivity().getApplication()).getAllRules()
                .observe(getViewLifecycleOwner(), rules -> {
                    TextView hint = root.findViewById(R.id.tv_recurring_hint);
                    if (rules != null && !rules.isEmpty() && hint != null) {
                        hint.setText("已设置 " + rules.size() + " 条");
                    }
                });

        // 设置各卡片点击事件
        setupCardClick(root);

        return root;
    }

    private void setupCardClick(View root) {
        root.findViewById(R.id.card_budget).setOnClickListener(v -> showBudgetDialog());
        root.findViewById(R.id.card_recurring).setOnClickListener(v -> showRecurringDialog());
        root.findViewById(R.id.card_category).setOnClickListener(v -> showCategoryManage());
        root.findViewById(R.id.card_export).setOnClickListener(v -> exportData());
        root.findViewById(R.id.card_sync).setOnClickListener(v -> doSync());
    }

    private void doSync() {
        SyncService sync = new SyncService(requireActivity().getApplication());
        Toast.makeText(requireContext(), "☁️ 同步中...", Toast.LENGTH_SHORT).show();
        sync.syncAll(new SyncService.SyncCallback() {
            @Override
            public void onSuccess(int count) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "✅ 同步完成！服务器共 " + count + " 条记录", Toast.LENGTH_SHORT).show();
                    TextView status = requireView().findViewById(R.id.tv_sync_status);
                    if (status != null) status.setText("已同步");
                });
            }

            @Override
            public void onError(String message) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "❌ " + message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showBudgetDialog() {
        try {
            Budget cur = null;
            try { cur = budgetRepo.getTotalBudgetSync(); } catch (Exception ignored) {}
            
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            final EditText et = new EditText(requireContext());
            et.setHint("月度预算金额（元）");
            et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            et.setPadding(48, 32, 48, 32);
            builder.setTitle("设置月度预算");
            builder.setView(et);

            if (cur != null && cur.getMonthlyLimit() > 0) {
                et.setText(String.valueOf((int) cur.getMonthlyLimit()));
                et.selectAll();
            }

            builder.setPositiveButton("保存", (d, w) -> {
                String s = et.getText().toString().trim();
                if (!s.isEmpty()) {
                    try {
                        saveBudget(Double.parseDouble(s));
                        Toast.makeText(requireContext(), "预算已设置", Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        Toast.makeText(requireContext(), "保存失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton("取消", null);
            builder.show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "无法打开预算设置", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveBudget(double limit) {
        Budget cur = budgetRepo.getTotalBudgetSync();
        if (cur != null) {
            cur.setMonthlyLimit(limit);
            budgetRepo.update(cur);
        } else {
            Budget b = new Budget();
            b.setCategoryId(null);
            b.setMonthlyLimit(limit);
            budgetRepo.insert(b);
        }
    }

    private void showRecurringDialog() {
        try {
            RecurringRepository repo = new RecurringRepository(requireActivity().getApplication());
            CategoryRepository catRepo = new CategoryRepository(requireActivity().getApplication());

            repo.getAllRules().observe(getViewLifecycleOwner(), rules -> {
                if (rules == null || rules.isEmpty()) {
                    showAddRecurringRuleDialog(repo);
                    return;
                }
                List<Category> cats = catRepo.getAllCategoriesSync();
                if (cats == null) cats = new ArrayList<>();
                String[] names = new String[rules.size()];
                String[] periods = {"每日", "每周", "每月", "每年"};
                for (int i = 0; i < rules.size(); i++) {
                    RecurringRule r = rules.get(i);
                    Category cat = null;
                    for (Category c : cats) {
                        if (c.getId() == r.getCategoryId()) { cat = c; break; }
                    }
                    String catName = cat != null ? cat.getName() : "分类#" + r.getCategoryId();
                    names[i] = catName + " " + String.format("%.0f", r.getAmount()) + " " + periods[r.getPeriodType()];
                }
                new AlertDialog.Builder(requireContext())
                        .setTitle("周期记账规则")
                        .setItems(names, null)
                        .setPositiveButton("添加规则", (d, w) -> showAddRecurringRuleDialog(repo))
                        .setNegativeButton("关闭", null)
                        .show();
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "无法打开周期记账", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddRecurringRuleDialog(RecurringRepository repo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        final EditText etAmount = new EditText(requireContext());
        etAmount.setHint("金额");
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        final EditText etNote = new EditText(requireContext());
        etNote.setHint("备注（如：房租）");
        final String[] periods = {"每日", "每周", "每月", "每年"};
        final int[] selectedPeriod = {2};

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);
        layout.addView(etAmount);
        layout.addView(etNote);

        TextView tvPeriod = new TextView(requireContext());
        tvPeriod.setText("周期：" + periods[selectedPeriod[0]]);
        tvPeriod.setPadding(0, 24, 0, 12);
        tvPeriod.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setItems(periods, (d, i) -> {
                        selectedPeriod[0] = i;
                        tvPeriod.setText("周期：" + periods[i]);
                    }).show();
        });
        layout.addView(tvPeriod);

        builder.setTitle("添加周期记账");
        builder.setView(layout);

        builder.setPositiveButton("保存", (d, w) -> {
            String amtStr = etAmount.getText().toString().trim();
            if (amtStr.isEmpty()) return;
            RecurringRule rule = new RecurringRule();
            rule.setCategoryId(0); // 默认分类（可用备注区分）
            rule.setAmount(Double.parseDouble(amtStr));
            rule.setType(0);
            rule.setNote(etNote.getText().toString().trim());
            rule.setPeriodType(selectedPeriod[0]);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.add(Calendar.MONTH, 1);
            rule.setNextDateMs(cal.getTimeInMillis());
            repo.insert(rule);
            Toast.makeText(requireContext(), "规则已添加", Toast.LENGTH_SHORT).show();
            // 刷新提示文字
            getView().postDelayed(() -> {
                TextView hint = getView().findViewById(R.id.tv_recurring_hint);
                if (hint != null) hint.setText("已设置");
            }, 300);
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showCategoryManage() {
        CategoryRepository catRepo = new CategoryRepository(requireActivity().getApplication());
        catRepo.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories == null || categories.isEmpty()) return;
            String[] names = new String[categories.size()];
            for (int i = 0; i < categories.size(); i++) {
                Category c = categories.get(i);
                String typeLabel = c.getType() == 0 ? "[支出]" : c.getType() == 1 ? "[收入]" : "[通用]";
                names[i] = c.getName() + " " + typeLabel;
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle("分类管理（长按删除）")
                    .setItems(names, null)
                    .setPositiveButton("添加分类", (d, w) -> showAddCategoryDialog())
                    .setNegativeButton("关闭", null)
                    .show();
        });
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        final EditText etName = new EditText(requireContext());
        etName.setHint("分类名称");
        etName.setPadding(48, 32, 48, 32);
        builder.setTitle("添加分类");
        builder.setView(etName);
        builder.setPositiveButton("保存", (d, w) -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) return;
            CategoryRepository catRepo = new CategoryRepository(requireActivity().getApplication());
            catRepo.insert(new Category(name, R.drawable.ic_cat_other, "#78909C", 0, null));
            Toast.makeText(requireContext(), "已添加", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void exportData() {
        try {
            TransactionRepository txRepo = new TransactionRepository(requireActivity().getApplication());
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_YEAR, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
            long yStart = cal.getTimeInMillis();
            cal.add(Calendar.YEAR, 1);
            long yEnd = cal.getTimeInMillis() - 1;

            List<com.easyaccount.app.data.entity.Transaction> list = txRepo.getTransactionsByDateRangeSync(yStart, yEnd);
            if (list == null || list.isEmpty()) {
                Toast.makeText(requireContext(), "暂无数据可导出", Toast.LENGTH_SHORT).show();
                return;
            }

            com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(list);
            java.io.File dir = new java.io.File(requireContext().getCacheDir(), "exports");
            dir.mkdirs();
            java.io.File file = new java.io.File(dir, "easyaccount_export.json");
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(json);
            writer.close();

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("application/json");
            share.putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider
                    .getUriForFile(requireContext(),
                            requireContext().getPackageName() + ".fileprovider", file));
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, "导出账单数据"));
            Toast.makeText(requireContext(), "已导出 " + list.size() + " 条记录", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "导出失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
