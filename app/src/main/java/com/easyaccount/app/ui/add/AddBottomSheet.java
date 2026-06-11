package com.easyaccount.app.ui.add;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.easyaccount.app.R;
import com.easyaccount.app.ai.AiConfig;
import com.easyaccount.app.ai.AiService;
import com.easyaccount.app.data.entity.Category;
import com.easyaccount.app.data.entity.Transaction;
import com.easyaccount.app.ui.home.HomeViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddBottomSheet extends BottomSheetDialogFragment {

    private Category category;
    private int type;
    private HomeViewModel vm;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    private final AiService aiService = new AiService();
    private ActivityResultLauncher<Intent> picker;
    private final long[] selMs = {System.currentTimeMillis()};

    public AddBottomSheet(Category category, int type, HomeViewModel vm) {
        this.category = category;
        this.type = type;
        this.vm = vm;
    }

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                                  @Nullable ViewGroup container, @Nullable Bundle s) {
        return inflater.inflate(R.layout.bottomsheet_add, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        picker = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if (uri != null) recognizeBill(uri);
            }
        });

        TextView title = v.findViewById(R.id.bs_title);
        title.setText(category.getName());

        EditText amount = v.findViewById(R.id.bs_amount);
        EditText note = v.findViewById(R.id.bs_note);
        TextView date = v.findViewById(R.id.bs_date);
        date.setText(sdf.format(new Date(selMs[0])));

        date.setOnClickListener(dv -> {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(selMs[0]);
            new DatePickerDialog(requireContext(), (p, y, m, d) -> {
                Calendar s1 = Calendar.getInstance();
                s1.set(y, m, d, 0, 0, 0);
                selMs[0] = s1.getTimeInMillis();
                date.setText(sdf.format(s1.getTime()));
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        v.findViewById(R.id.bs_ai).setOnClickListener(bv -> {
            if (AiConfig.API_KEY.equals("YOUR_ZHIPU_API_KEY_HERE")) {
                Toast.makeText(requireContext(), "请先在 AiConfig.java 中填写 API Key", Toast.LENGTH_LONG).show();
                return;
            }
            picker.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        });

        v.findViewById(R.id.bs_save).setOnClickListener(bv -> {
            String amt = amount.getText().toString().trim();
            if (amt.isEmpty()) { Toast.makeText(requireContext(), "请输入金额", Toast.LENGTH_SHORT).show(); return; }
            Transaction tx = new Transaction();
            tx.setAmount(Double.parseDouble(amt));
            tx.setType(type);
            tx.setCategoryId(category.getId());
            tx.setNote(note.getText().toString().trim());
            tx.setDateMs(selMs[0]);
            vm.insert(tx);
            vm.refreshMonthlyData();
            Toast.makeText(requireContext(), "已保存", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    private void recognizeBill(Uri uri) {
        try {
            Bitmap bmp = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
            Toast.makeText(requireContext(), "🤖 识别中...", Toast.LENGTH_SHORT).show();
            aiService.recognizeBill(bmp, new AiService.AiCallback() {
                @Override public void onSuccess(double amount, String merchant, String d) {
                    requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "识别: " + merchant + " " + amount, Toast.LENGTH_LONG).show());
                }
                @Override public void onError(String msg) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show());
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "图片读取失败", Toast.LENGTH_SHORT).show();
        }
    }
}
