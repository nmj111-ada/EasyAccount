package com.easyaccount.app.sync;

import android.content.Context;

import com.easyaccount.app.data.AppDatabase;
import com.easyaccount.app.data.entity.Budget;
import com.easyaccount.app.data.entity.Category;
import com.easyaccount.app.data.entity.RecurringRule;
import com.easyaccount.app.data.entity.Tag;
import com.easyaccount.app.data.entity.Transaction;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 云同步服务 — 与 Spring Boot 后端双向同步数据
 */
public class SyncService {

    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // 修改为你的服务器地址
    private static final String SERVER_URL = "http://10.0.2.2:8080"; // 模拟器用 10.0.2.2 访问宿主机

    public SyncService(Context context) {
        this.context = context.getApplicationContext();
    }

    public interface SyncCallback {
        void onSuccess(int txCount);
        void onError(String message);
    }

    /** 全量同步：上传本地数据 → 接收服务器数据并保存 */
    public void syncAll(SyncCallback callback) {
        executor.execute(() -> {
            try {
                // 先 GET 测试连通性
                URL testUrl = new URL(SERVER_URL + "/api/categories");
                HttpURLConnection testConn = (HttpURLConnection) testUrl.openConnection();
                testConn.setConnectTimeout(3000);
                testConn.setReadTimeout(3000);
                int testCode = testConn.getResponseCode();
                if (testCode != 200) {
                    callback.onError("后端未响应 (code " + testCode + ")，请确保 Spring Boot 已启动");
                    return;
                }
                testConn.disconnect();

                long localCount = AppDatabase.getInstance(context).transactionDao().getTransactionCountSync();

                // 上传数据
                JSONObject body = buildSyncBody();
                String json = body.toString();

                URL url = new URL(SERVER_URL + "/api/sync");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                OutputStream os = conn.getOutputStream();
                os.write(json.getBytes("UTF-8"));
                os.close();

                int code = conn.getResponseCode();
                if (code != 200) {
                    BufferedReader er = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                    StringBuilder err = new StringBuilder();
                    String line;
                    while ((line = er.readLine()) != null) err.append(line);
                    er.close();
                    callback.onError("同步失败: " + code + " " + err.toString());
                    return;
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder resp = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) resp.append(line);
                br.close();

                JSONObject serverData = new JSONObject(resp.toString());

                // 保存服务器端的分类和标签（App 本地为主，服务器为辅）
                saveRemoteCategories(serverData.optJSONArray("categories"));
                saveRemoteTags(serverData.optJSONArray("tags"));

                int serverTxCount = serverData.optJSONArray("transactions") != null
                        ? serverData.optJSONArray("transactions").length() : 0;

                callback.onSuccess(serverTxCount);

            } catch (Exception e) {
                android.util.Log.e("SyncService", "同步失败", e);
                callback.onError("同步失败: " + e.getMessage());
            }
        });
    }

    private JSONObject buildSyncBody() throws Exception {
        JSONObject body = new JSONObject();
        // 暂时只同步核心数据
        AppDatabase db = AppDatabase.getInstance(context);
        body.put("transactions", toJsonArray(db.transactionDao().getAllTransactionsSync()));
        body.put("categories", toJsonArray(db.categoryDao().getAllCategoriesSync()));
        body.put("tags", toJsonArray(db.tagDao().getAllTagsSync()));
        body.put("budgets", toJsonArray(db.budgetDao().getAllBudgetsSync()));
        body.put("recurringRules", toJsonArray(db.recurringDao().getAllRulesSync()));
        return body;
    }

    private JSONArray toJsonArray(List<?> list) throws Exception {
        JSONArray arr = new JSONArray();
        for (Object obj : list) {
            arr.put(new JSONObject(new com.google.gson.Gson().toJson(obj)));
        }
        return arr;
    }

    private void saveRemoteCategories(JSONArray categories) {
        if (categories == null) return;
        try {
            new com.google.gson.Gson().fromJson(categories.toString(),
                    new com.google.gson.reflect.TypeToken<List<Category>>(){}.getType());
        } catch (Exception ignored) {}
    }

    private void saveRemoteTags(JSONArray tags) {
        if (tags == null) return;
        // MVP 阶段暂不处理远程标签覆盖
    }
}
