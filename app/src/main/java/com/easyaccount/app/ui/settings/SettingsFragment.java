package com.easyaccount.app.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.easyaccount.app.R;
import com.easyaccount.app.data.entity.Category;
import com.easyaccount.app.data.entity.Transaction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;
    private ListView categoryList;
    private CategoryAdapter adapter;
    private Switch darkModeSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        categoryList = root.findViewById(R.id.settings_category_list);
        darkModeSwitch = root.findViewById(R.id.switch_dark_mode);
        Button btnAddCategory = root.findViewById(R.id.btn_add_category);
        Button btnExport = root.findViewById(R.id.btn_export);
        TextView tvVersion = root.findViewById(R.id.tv_version);

        // 设置版本号
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
            // 实时切换深色模式
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    isChecked ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                            : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            );
            requireActivity().recreate();
        });

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        btnExport.setOnClickListener(v -> exportData());

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this,
                new SettingsViewModel.Factory(requireActivity().getApplication())).get(SettingsViewModel.class);

        adapter = new CategoryAdapter(requireContext(), new ArrayList<>());
        categoryList.setAdapter(adapter);

        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            adapter.clear();
            if (categories != null) {
                adapter.addAll(categories);
            }
        });

        // 点击编辑、长按删除
        categoryList.setOnItemClickListener((parent, view1, position, id) -> {
            Category category = adapter.getItem(position);
            if (category != null) {
                showEditCategoryDialog(category);
            }
        });

        categoryList.setOnItemLongClickListener((parent, view1, position, id) -> {
            Category category = adapter.getItem(position);
            if (category != null) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("删除分类")
                        .setMessage("确定删除「" + category.getName() + "」吗？")
                        .setPositiveButton("删除", (dialog, which) -> viewModel.deleteCategory(category))
                        .setNegativeButton("取消", null)
                        .show();
                return true;
            }
            return false;
        });
    }

    private void showAddCategoryDialog() {
        showCategoryDialog(null);
    }

    private void showEditCategoryDialog(Category category) {
        showCategoryDialog(category);
    }

    // 预设 emoji 图标列表
    private static final String[] ICON_EMOJIS = {
            "🍔", "🍜", "🍰", "☕", "🍺", "🍕", "🥤", "🍱",
            "🚗", "🚌", "🚇", "✈️", "🚲", "⛽", "🚕", "🛵",
            "🛒", "👗", "💄", "🎮", "🎬", "🎵", "📚", "🏀",
            "🏠", "💡", "💧", "📱", "🌐", "🏥", "💊", "💉",
            "💰", "💳", "🏦", "💵", "🎁", "🎓", "🐱", "🌟"
    };

    // 预设颜色列表
    private static final String[] PRESET_COLORS = {
            "#FF7043", "#42A5F5", "#AB47BC", "#26A69A",
            "#5C6BC0", "#EF5350", "#66BB6A", "#FFA726",
            "#78909C", "#8D6E63", "#EC407A", "#29B6F6"
    };

    private void showCategoryDialog(@Nullable Category existing) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_category, null);
        builder.setView(view);

        EditText etName = view.findViewById(R.id.et_category_name);
        TextView tvIconPreview = view.findViewById(R.id.tv_category_icon_preview);
        GridView gridIcons = view.findViewById(R.id.grid_icons);
        LinearLayout llColors = view.findViewById(R.id.ll_colors);

        String title = existing != null ? "编辑分类" : "添加分类";
        builder.setTitle(title);

        // 当前选中的图标和颜色
        final int[] selectedIconIndex = {0};
        final String[] selectedColor = {existing != null ? existing.getColor() : "#78909C"};

        if (existing != null) etName.setText(existing.getName());

        // 设置图标网格
        gridIcons.setAdapter(new BaseAdapter() {
            @Override public int getCount() { return ICON_EMOJIS.length; }
            @Override public Object getItem(int i) { return ICON_EMOJIS[i]; }
            @Override public long getItemId(int i) { return i; }
            @Override
            public View getView(int i, View v, ViewGroup parent) {
                TextView tv = v != null ? (TextView) v : new TextView(requireContext());
                if (v == null) { tv.setGravity(Gravity.CENTER); tv.setPadding(6,6,6,6); tv.setTextSize(22f); }
                tv.setText(ICON_EMOJIS[i]);
                return tv;
            }
        });

        gridIcons.setOnItemClickListener((parent, v, pos, id) -> {
            selectedIconIndex[0] = pos;
            tvIconPreview.setText(ICON_EMOJIS[pos]);
        });

        // 设置颜色点
        for (int i = 0; i < PRESET_COLORS.length; i++) {
            final String colorStr = PRESET_COLORS[i];
            View dot = new View(requireContext());
            int size = (int) (36 * requireContext().getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);

            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            try {
                shape.setColor(Color.parseColor(colorStr));
            } catch (Exception e) {
                shape.setColor(Color.GRAY);
            }
            // 标记当前选中的颜色
            if (colorStr.equals(selectedColor[0])) {
                shape.setStroke(4, Color.BLACK);
            } else {
                shape.setStroke(2, Color.LTGRAY);
            }
            dot.setBackground(shape);

            final int index = i;
            dot.setOnClickListener(dv -> {
                selectedColor[0] = PRESET_COLORS[index];
                // 刷新所有颜色点的边框
                for (int j = 0; j < llColors.getChildCount(); j++) {
                    View child = llColors.getChildAt(j);
                    GradientDrawable gd = (GradientDrawable) child.getBackground();
                    if (j == index) {
                        gd.setStroke(4, Color.BLACK);
                    } else {
                        gd.setStroke(2, Color.LTGRAY);
                    }
                }
            });

            llColors.addView(dot);
        }

        builder.setPositiveButton("保存", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "请输入分类名称", Toast.LENGTH_SHORT).show();
                return;
            }
            if (existing != null) {
                existing.setName(name);
                existing.setIconRes(R.drawable.ic_cat_other);
                existing.setColor(selectedColor[0]);
                viewModel.updateCategory(existing);
            } else {
                Category cat = new Category(name, R.drawable.ic_cat_other, selectedColor[0], 0, null);
                viewModel.insertCategory(cat);
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void exportData() {
        // 计算今年的时间范围
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long yearStart = cal.getTimeInMillis();

        cal.add(Calendar.YEAR, 1);
        long yearEnd = cal.getTimeInMillis() - 1;

        // 先显示提示
        Toast.makeText(requireContext(), "正在导出...", Toast.LENGTH_SHORT).show();

        // 在后台线程执行数据库查询和文件写入
        new Thread(() -> {
            List<Transaction> transactions = viewModel.exportAllTransactions(yearStart, yearEnd);

            // 回到主线程处理 UI
            new Handler(Looper.getMainLooper()).post(() -> {
                if (transactions == null || transactions.isEmpty()) {
                    Toast.makeText(requireContext(), "暂无数据可导出", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    String json = gson.toJson(transactions);

                    File cacheDir = new File(requireContext().getCacheDir(), "exports");
                    cacheDir.mkdirs();
                    File file = new File(cacheDir, "easyaccount_export.json");
                    FileWriter writer = new FileWriter(file);
                    writer.write(json);
                    writer.close();

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("application/json");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider
                            .getUriForFile(requireContext(),
                                    requireContext().getPackageName() + ".fileprovider",
                                    file));
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(shareIntent, "导出账单数据"));

                    Toast.makeText(requireContext(),
                            "已导出 " + transactions.size() + " 条记录", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    Toast.makeText(requireContext(), "导出失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private static class CategoryAdapter extends ArrayAdapter<Category> {
        public CategoryAdapter(@NonNull Context context, @NonNull List<Category> objects) {
            super(context, android.R.layout.simple_list_item_2, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(android.R.layout.simple_list_item_2, parent, false);
            }
            Category cat = getItem(position);
            if (cat != null) {
                TextView text1 = convertView.findViewById(android.R.id.text1);
                TextView text2 = convertView.findViewById(android.R.id.text2);
                text1.setText(cat.getName());
                text2.setText(cat.getType() == 0 ? "支出" : "收入");
            }
            return convertView;
        }
    }
}
