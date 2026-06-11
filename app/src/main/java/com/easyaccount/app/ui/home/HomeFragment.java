package com.easyaccount.app.ui.home;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.easyaccount.app.R;
import com.easyaccount.app.ai.AiConfig;
import com.easyaccount.app.ai.AiService;
import com.easyaccount.app.data.entity.Category;
import com.easyaccount.app.data.entity.Tag;
import com.easyaccount.app.data.entity.Transaction;
import com.easyaccount.app.ui.adapter.TransactionAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private TransactionAdapter adapter;
    private RecyclerView recyclerView;
    private TextView tvMonthlyIncome, tvMonthlyExpense, tvBalance, tvEmpty;

    private final AiService aiService = new AiService();
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        tvMonthlyIncome = root.findViewById(R.id.tv_monthly_income);
        tvMonthlyExpense = root.findViewById(R.id.tv_monthly_expense);
        tvBalance = root.findViewById(R.id.tv_balance);
        tvEmpty = root.findViewById(R.id.tv_empty);
        recyclerView = root.findViewById(R.id.recycler_transactions);

        // 设置 RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TransactionAdapter(transaction -> {
            // 点击编辑
            showEditTransactionDialog(transaction);
        });
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化图片选择器（用于 AI 识别）
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            recognizeFromImage(imageUri);
                        }
                    }
                });

        viewModel = new ViewModelProvider(this,
                new HomeViewModel.Factory(requireActivity().getApplication())).get(HomeViewModel.class);

        // 观察当月账单
        viewModel.getCurrentMonthTransactions().observe(getViewLifecycleOwner(), transactions -> {
            adapter.submitList(transactions);
            tvEmpty.setVisibility(transactions == null || transactions.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // 观察月度统计
        viewModel.getMonthlyExpense().observe(getViewLifecycleOwner(), expense -> {
            if (expense != null) {
                tvMonthlyExpense.setText(String.format("¥%.2f", expense));
                updateBalance();
            }
        });

        viewModel.getMonthlyIncome().observe(getViewLifecycleOwner(), income -> {
            if (income != null) {
                tvMonthlyIncome.setText(String.format("¥%.2f", income));
                updateBalance();
            }
        });

        // 观察所有分类，传给 Adapter 用于显示分类名
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            adapter.setCategories(categories);
        });

        // 观察预算
        viewModel.getTotalBudget().observe(getViewLifecycleOwner(), budget -> {
            Double expense = viewModel.getMonthlyExpense().getValue();
            if (budget != null && expense != null && budget.getMonthlyLimit() > 0) {
                if (expense > budget.getMonthlyLimit() && budget.isNotifyOnExceed()) {
                    tvMonthlyExpense.setText(String.format("¥%.2f ⚠超支", expense));
                }
            }
        });
    }

    private void updateBalance() {
        Double income = viewModel.getMonthlyIncome().getValue();
        Double expense = viewModel.getMonthlyExpense().getValue();
        if (income != null && expense != null) {
            double balance = income - expense;
            tvBalance.setText(String.format("¥%.2f", balance));
        }
    }

    /** 显示添加账单对话框 */
    public void showAddTransactionDialog() {
        showTransactionDialog(null);
    }

    /** 显示编辑账单对话框 */
    private void showEditTransactionDialog(Transaction transaction) {
        showTransactionDialog(transaction);
    }

    private void showTransactionDialog(@Nullable Transaction existing) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_transaction, null);
        builder.setView(dialogView);

        EditText etAmount = dialogView.findViewById(R.id.et_amount);
        EditText etNote = dialogView.findViewById(R.id.et_note);
        TextView tvDate = dialogView.findViewById(R.id.tv_date);
        TextView tvCategory = dialogView.findViewById(R.id.tv_category);
        RadioGroup rgType = dialogView.findViewById(R.id.rg_type);
        RadioButton rbExpense = dialogView.findViewById(R.id.rb_expense);
        RadioButton rbIncome = dialogView.findViewById(R.id.rb_income);
        View hsvTags = dialogView.findViewById(R.id.hsv_tags);
        android.widget.LinearLayout llTags = dialogView.findViewById(R.id.ll_tags);
        com.google.android.material.button.MaterialButton btnAi = dialogView.findViewById(R.id.btn_ai_recognize);

        // 默认当前日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        final long[] selectedDateMs = {System.currentTimeMillis()};
        tvDate.setText(sdf.format(new Date(selectedDateMs[0])));

        // 默认选中的分类和标签
        final long[] selectedCategoryId = {-1};
        final long[] selectedTagId = {-1};
        final int[] selectedType = {0}; // 默认支出
        tvCategory.setText("请选择分类");

        // AI 拍照识别按钮
        btnAi.setOnClickListener(v -> {
            if (AiConfig.API_KEY.equals("YOUR_ZHIPU_API_KEY_HERE")) {
                Toast.makeText(requireContext(), "请先在 AiConfig.java 中填写 API Key", Toast.LENGTH_LONG).show();
                return;
            }
            Intent pickIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(pickIntent);
        });

        // 日期选择
        tvDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selectedDateMs[0]);
            DatePickerDialog picker = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, dayOfMonth, 0, 0, 0);
                        selectedDateMs[0] = selected.getTimeInMillis();
                        tvDate.setText(sdf.format(selected.getTime()));
                    },
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            picker.show();
        });

        // 分类选择（同时加载该分类的标签）
        tvCategory.setOnClickListener(v -> showCategoryPicker(selectedType[0], categoryId -> {
            selectedCategoryId[0] = categoryId;
            selectedTagId[0] = -1;
            viewModel.getCategoryById(categoryId).observe(getViewLifecycleOwner(), category -> {
                if (category != null) {
                    tvCategory.setText(category.getName());
                }
            });
            // 加载该分类下的标签
            loadTagsForCategory(categoryId, llTags, hsvTags, selectedTagId);
        }));

        // 类型切换
        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_income) {
                selectedType[0] = 1;
            } else {
                selectedType[0] = 0;
            }
            selectedCategoryId[0] = -1;
            selectedTagId[0] = -1;
            tvCategory.setText("请选择分类");
            hsvTags.setVisibility(View.GONE);
        });

        // 如果是编辑，填充已有数据
        if (existing != null) {
            etAmount.setText(String.valueOf(existing.getAmount()));
            etNote.setText(existing.getNote());
            selectedDateMs[0] = existing.getDateMs();
            tvDate.setText(sdf.format(new Date(existing.getDateMs())));
            selectedType[0] = existing.getType();
            selectedCategoryId[0] = existing.getCategoryId();
            if (existing.getType() == 1) {
                rbIncome.setChecked(true);
            } else {
                rbExpense.setChecked(true);
            }
            // 加载分类名
            viewModel.getCategoryById(existing.getCategoryId()).observe(getViewLifecycleOwner(), category -> {
                if (category != null) {
                    tvCategory.setText(category.getName());
                }
            });
        }

        builder.setPositiveButton("保存", (dialog, which) -> {
            String amountStr = etAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "请输入金额", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedCategoryId[0] == -1) {
                Toast.makeText(requireContext(), "请选择分类", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            Transaction tx = existing != null ? existing : new Transaction();
            tx.setAmount(amount);
            tx.setType(selectedType[0]);
            tx.setCategoryId(selectedCategoryId[0]);
            tx.setNote(etNote.getText().toString().trim());
            tx.setDateMs(selectedDateMs[0]);

            if (existing != null) {
                viewModel.update(tx);
                // 更新标签关联
                if (selectedTagId[0] > 0) {
                    java.util.List<Long> ids = new java.util.ArrayList<>();
                    ids.add(selectedTagId[0]);
                    viewModel.setTransactionTags(existing.getId(), ids);
                } else {
                    viewModel.setTransactionTags(existing.getId(), new java.util.ArrayList<>());
                }
            } else {
                viewModel.insert(tx, newId -> {
                    if (selectedTagId[0] > 0) {
                        viewModel.addTagToTransaction(newId, selectedTagId[0]);
                    }
                });
            }

            // 刷新月度统计
            viewModel.refreshMonthlyData();
        });

        builder.setNegativeButton("取消", null);

        // 编辑模式显示删除按钮
        if (existing != null) {
            builder.setNeutralButton("删除", (dialog, which) -> {
                new AlertDialog.Builder(requireContext())
                        .setMessage("确认删除此账单？")
                        .setPositiveButton("确认", (d, w) -> {
                            viewModel.delete(existing);
                            viewModel.refreshMonthlyData();
                        })
                        .setNegativeButton("取消", null)
                        .show();
            });
        }

        builder.show();
    }

    private void showCategoryPicker(int type, CategoryPickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("选择分类");

        viewModel.getCategoriesByType(type).observe(getViewLifecycleOwner(), categories -> {
            if (categories == null || categories.isEmpty()) return;

            String[] names = new String[categories.size()];
            for (int i = 0; i < categories.size(); i++) {
                names[i] = categories.get(i).getName();
            }

            builder.setItems(names, (dialog, which) -> {
                listener.onPick(categories.get(which).getId());
            });
            builder.setNegativeButton("取消", null);
            builder.show();
        });
    }

    interface CategoryPickListener {
        void onPick(long categoryId);
    }

    /** 根据分类加载标签芯片 */
    private void loadTagsForCategory(long categoryId,
                                      LinearLayout llTags,
                                      View hsvTags,
                                      final long[] selectedTagId) {
        llTags.removeAllViews();
        viewModel.getTagsByCategory(categoryId).observe(getViewLifecycleOwner(), tags -> {
            llTags.removeAllViews();
            if (tags == null || tags.isEmpty()) {
                hsvTags.setVisibility(View.GONE);
                return;
            }
            hsvTags.setVisibility(View.VISIBLE);
            int padding = (int) (8 * getResources().getDisplayMetrics().density);
            for (Tag tag : tags) {
                TextView chip = new TextView(requireContext());
                chip.setText(tag.getName());
                chip.setTextSize(13);
                chip.setPadding(padding, padding / 2, padding, padding / 2);
                chip.setTextColor(requireContext().getColor(R.color.on_primary));
                chip.setBackgroundColor(android.graphics.Color.parseColor(
                        tag.getColor() != null ? tag.getColor() : "#78909C"));
                android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
                bg.setCornerRadius(16 * getResources().getDisplayMetrics().density);
                bg.setColor(android.graphics.Color.parseColor(
                        tag.getColor() != null ? tag.getColor() : "#78909C"));
                chip.setBackground(bg);
                chip.setClickable(true);
                chip.setOnClickListener(cv -> {
                    selectedTagId[0] = tag.getId();
                    // 高亮选中
                    for (int i = 0; i < llTags.getChildCount(); i++) {
                        View child = llTags.getChildAt(i);
                        child.setAlpha(child == chip ? 1.0f : 0.4f);
                    }
                });

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, padding, 0);
                chip.setLayoutParams(params);
                llTags.addView(chip);
            }
        });
    }

    /** AI 识别图片并显示结果 */
    private void recognizeFromImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    requireContext().getContentResolver(), imageUri);
            Toast.makeText(requireContext(), "🤖 识别中...", Toast.LENGTH_SHORT).show();

            aiService.recognizeBill(bitmap, new AiService.AiCallback() {
                @Override
                public void onSuccess(double amount, String merchant, String dateStr) {
                    requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                "识别成功: " + merchant + " " + amount + " " + dateStr,
                                Toast.LENGTH_LONG).show()
                    );
                }

                @Override
                public void onError(String message) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show());
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "图片读取失败", Toast.LENGTH_SHORT).show();
        }
    }
}
