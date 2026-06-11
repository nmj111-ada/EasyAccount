package com.easyaccount.app.ui.add;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.easyaccount.app.R;
import com.easyaccount.app.ai.AiConfig;
import com.easyaccount.app.ai.AiService;
import com.easyaccount.app.data.entity.Category;
import com.easyaccount.app.data.entity.Transaction;
import com.easyaccount.app.ui.home.HomeViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTransactionFragment extends Fragment {

    private HomeViewModel viewModel;
    private RecyclerView rvGrid;
    private TextView tabExpense, tabIncome;
    private int selectedType = 0;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private final AiService aiService = new AiService();
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) recognizeBill(uri);
                    }
                });

        viewModel = new ViewModelProvider(requireActivity(),
                new HomeViewModel.Factory(requireActivity().getApplication())).get(HomeViewModel.class);

        tabExpense = view.findViewById(R.id.tab_expense);
        tabIncome = view.findViewById(R.id.tab_income);
        rvGrid = view.findViewById(R.id.rv_category_grid);
        rvGrid.setLayoutManager(new GridLayoutManager(requireContext(), 4));

        CatAdapter adapter = new CatAdapter(cat -> showInputSheet(cat));
        rvGrid.setAdapter(adapter);

        tabExpense.setOnClickListener(v -> switchTab(0, adapter));
        tabIncome.setOnClickListener(v -> switchTab(1, adapter));

        viewModel.getCategoriesByType(0).observe(getViewLifecycleOwner(), cats -> {
            if (selectedType == 0) adapter.setData(cats);
        });
        viewModel.getCategoriesByType(1).observe(getViewLifecycleOwner(), cats -> {
            if (selectedType == 1) adapter.setData(cats);
        });

        switchTab(0, adapter);
    }

    private void switchTab(int type, CatAdapter adapter) {
        selectedType = type;
        tabExpense.setBackgroundColor(type == 0 ?
                getResources().getColor(R.color.primary) : Color.TRANSPARENT);
        tabExpense.setTextColor(type == 0 ?
                getResources().getColor(R.color.on_primary) : getResources().getColor(R.color.text_secondary));
        tabIncome.setBackgroundColor(type == 1 ?
                getResources().getColor(R.color.primary) : Color.TRANSPARENT);
        tabIncome.setTextColor(type == 1 ?
                getResources().getColor(R.color.on_primary) : getResources().getColor(R.color.text_secondary));
        viewModel.getCategoriesByType(type).observe(getViewLifecycleOwner(), adapter::setData);
    }

    private void showInputSheet(Category category) {
        new AddBottomSheet(category, selectedType, viewModel)
                .show(getParentFragmentManager(), "add_bottom_sheet");
    }

    private void recognizeBill(Uri uri) {
        try {
            Bitmap bmp = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
            Toast.makeText(requireContext(), "🤖 识别中...", Toast.LENGTH_SHORT).show();
            aiService.recognizeBill(bmp, new AiService.AiCallback() {
                @Override public void onSuccess(double amount, String merchant, String dateStr) {
                    requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "识别: " + merchant + " " + amount, Toast.LENGTH_LONG).show());
                }
                @Override public void onError(String msg) {
                    requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show());
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "图片读取失败", Toast.LENGTH_SHORT).show();
        }
    }

    private static class CatAdapter extends RecyclerView.Adapter<CatAdapter.VH> {
        private List<Category> data = new ArrayList<>();
        private final CatClick click;

        CatAdapter(CatClick c) { this.click = c; }
        void setData(List<Category> d) { data = d != null ? d : new ArrayList<>(); notifyDataSetChanged(); }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int wt) {
            // FrameLayout wrapper for ripple
            android.widget.FrameLayout wrap = new android.widget.FrameLayout(p.getContext());
            int size = p.getWidth() / 4 - 40;
            wrap.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            wrap.setPadding(12, 16, 12, 16);
            wrap.setClickable(true);
            wrap.setFocusable(true);
            int[] attrs = {android.R.attr.selectableItemBackground};
            android.content.res.TypedArray ta = p.getContext().obtainStyledAttributes(attrs);
            wrap.setForeground(ta.getDrawable(0));
            ta.recycle();

            TextView tv = new TextView(p.getContext());
            tv.setLayoutParams(new android.widget.FrameLayout.LayoutParams(size, size, Gravity.CENTER));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(13f);
            tv.setPadding(12, 12, 12, 12);
            wrap.addView(tv);
            return new VH(wrap, tv);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Category cat = data.get(pos);
            String colorStr = cat.getColor() != null ? cat.getColor() : "#E0E0E0";
            int bgColor;
            try { bgColor = Color.parseColor(colorStr); }
            catch (Exception e) { bgColor = Color.parseColor("#E0E0E0"); }
            int alpha = (bgColor & 0x00FFFFFF) | 0x3F000000;
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(24);
            bg.setColor(alpha);
            h.tv.setBackground(bg);
            h.tv.setText(cat.getName());
            int txtColor;
            try { txtColor = Color.parseColor(colorStr); }
            catch (Exception e) { txtColor = Color.GRAY; }
            h.tv.setTextColor(txtColor);
            h.itemView.setOnClickListener(v -> click.onClick(cat));
        }

        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tv;
            VH(View v, TextView tv) { super(v); this.tv = tv; }
        }
    }

    interface CatClick { void onClick(Category cat); }
}
