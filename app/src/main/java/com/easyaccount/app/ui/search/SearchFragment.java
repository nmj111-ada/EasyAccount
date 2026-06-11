package com.easyaccount.app.ui.search;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.easyaccount.app.R;
import com.easyaccount.app.data.entity.Category;
import com.easyaccount.app.ui.adapter.TransactionAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SearchFragment extends Fragment {

    private SearchViewModel viewModel;
    private TransactionAdapter adapter;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private EditText etSearch;
    private Spinner spinnerCategory;
    private TextView tvDateRange;

    private long dateStartMs, dateEndMs;
    private List<Category> allCategories = new ArrayList<>();
    private final Map<Long, Category> categoryMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        etSearch = root.findViewById(R.id.et_search);
        spinnerCategory = root.findViewById(R.id.spinner_category_filter);
        tvDateRange = root.findViewById(R.id.tv_search_date_range);
        tvEmpty = root.findViewById(R.id.tv_search_empty);
        recyclerView = root.findViewById(R.id.recycler_search_results);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TransactionAdapter(tx -> {});
        recyclerView.setAdapter(adapter);

        // 默认日期范围：近一年
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        dateStartMs = cal.getTimeInMillis();
        dateEndMs = System.currentTimeMillis();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this,
                new SearchViewModel.Factory(requireActivity().getApplication()))
                .get(SearchViewModel.class);

        // 加载分类列表
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), cats -> {
            allCategories.clear();
            categoryMap.clear();
            allCategories.add(null); // "全部分类" 占位
            if (cats != null) {
                allCategories.addAll(cats);
                for (Category c : cats) categoryMap.put(c.getId(), c);
            }
            String[] names = new String[allCategories.size()];
            names[0] = "全部分类";
            for (int i = 1; i < allCategories.size(); i++) {
                Category c = allCategories.get(i);
                names[i] = c.getName();
            }
            spinnerCategory.setAdapter(new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, names));
        });

        // 分类筛选
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                doSearch();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        // 关键词搜索（实时）
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { doSearch(); }
        });

        // 日期范围
        tvDateRange.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(dateStartMs);
            DatePickerDialog picker = new DatePickerDialog(requireContext(),
                    (view1, year, month, day) -> {
                        Calendar s = Calendar.getInstance();
                        s.set(year, month, day, 0, 0, 0);
                        dateStartMs = s.getTimeInMillis();
                        Calendar eCal = Calendar.getInstance();
                        eCal.set(Calendar.HOUR_OF_DAY, 23);
                        eCal.set(Calendar.MINUTE, 59);
                        dateEndMs = eCal.getTimeInMillis();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                        tvDateRange.setText(sdf.format(new Date(dateStartMs)) + " ~ " +
                                sdf.format(new Date(dateEndMs)));
                        doSearch();
                    },
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            picker.show();
        });
    }

    private void doSearch() {
        String keyword = etSearch.getText().toString().trim();
        int catPos = spinnerCategory.getSelectedItemPosition();
        Long catId = (catPos > 0 && catPos < allCategories.size()) ?
                allCategories.get(catPos).getId() : null;

        if (catId != null) {
            viewModel.searchByCategoryAndDate(catId, dateStartMs, dateEndMs)
                    .observe(getViewLifecycleOwner(), this::showResults);
        } else if (!keyword.isEmpty()) {
            viewModel.searchByKeyword(keyword).observe(getViewLifecycleOwner(), this::showResults);
        } else {
            viewModel.searchByDateRange(dateStartMs, dateEndMs)
                    .observe(getViewLifecycleOwner(), this::showResults);
        }
    }

    private void showResults(List<com.easyaccount.app.data.entity.Transaction> transactions) {
        adapter.setCategories(new ArrayList<>(categoryMap.values()));
        adapter.submitList(transactions);
        boolean empty = transactions == null || transactions.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
