package com.easyaccount.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.easyaccount.app.R;
import com.easyaccount.app.data.entity.Category;
import com.easyaccount.app.data.entity.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionAdapter extends ListAdapter<Transaction, TransactionAdapter.ViewHolder> {

    private final OnItemClickListener onItemClick;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("MM/dd E HH:mm", Locale.CHINA);
    private final Map<Long, Category> categoryMap = new HashMap<>();

    public void setCategories(List<Category> categories) {
        categoryMap.clear();
        if (categories != null) {
            for (Category cat : categories) {
                categoryMap.put(cat.getId(), cat);
            }
        }
        notifyDataSetChanged();
    }

    public TransactionAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<Transaction>() {
            @Override
            public boolean areItemsTheSame(@NonNull Transaction old, @NonNull Transaction newItem) {
                return old.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Transaction old, @NonNull Transaction newItem) {
                return old.getAmount() == newItem.getAmount()
                        && old.getType() == newItem.getType()
                        && old.getCategoryId() == newItem.getCategoryId()
                        && old.getDateMs() == newItem.getDateMs()
                        && (old.getNote() != null ? old.getNote().equals(newItem.getNote()) : newItem.getNote() == null);
            }
        });
        this.onItemClick = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction tx = getItem(position);

        holder.tvDate.setText(timeFormat.format(new Date(tx.getDateMs())));

        String amountStr = String.format("%.2f", tx.getAmount());
        if (tx.getType() == 0) {
            holder.tvAmount.setText("-" + amountStr);
            holder.tvAmount.setTextColor(holder.itemView.getContext().getColor(R.color.expense_color));
        } else {
            holder.tvAmount.setText("+" + amountStr);
            holder.tvAmount.setTextColor(holder.itemView.getContext().getColor(R.color.income_color));
        }

        Category category = categoryMap.get(tx.getCategoryId());
        String note = tx.getNote();
        boolean hasNote = note != null && !note.isEmpty();

        if (hasNote) {
            holder.tvCategory.setText(note);
            if (category != null) {
                holder.tvNote.setText("· " + category.getName());
            } else {
                holder.tvNote.setText("");
            }
        } else {
            if (category != null) {
                holder.tvCategory.setText(category.getName());
            } else {
                holder.tvCategory.setText(categoryMap.isEmpty() ? "加载中..." : "分类#" + tx.getCategoryId());
            }
            holder.tvNote.setText("");
        }

        holder.itemView.setOnClickListener(v -> onItemClick.onClick(tx));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvCategory, tvNote, tvAmount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_item_date);
            tvCategory = itemView.findViewById(R.id.tv_item_category);
            tvNote = itemView.findViewById(R.id.tv_item_note);
            tvAmount = itemView.findViewById(R.id.tv_item_amount);
        }
    }

    public interface OnItemClickListener {
        void onClick(Transaction transaction);
    }
}
