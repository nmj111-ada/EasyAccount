package com.easyaccount.app.ai;

import android.graphics.Bitmap;
import android.util.Base64;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI 智能记账服务 — 调智谱 GLM-4V-Flash 识别账单截图
 * MVP 版：上传图片 → 提取金额/商户/日期
 */
public class AiService {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface AiCallback {
        void onSuccess(double amount, String merchant, String dateStr);
        void onError(String message);
    }

    /**
     * 识别账单截图
     * @param bitmap 用户选择的图片
     * @param callback 回调
     */
    public void recognizeBill(Bitmap bitmap, AiCallback callback) {
        executor.execute(() -> {
            try {
                // 1. 将 Bitmap 压缩为 JPEG 并转 Base64
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] imageBytes = baos.toByteArray();
                String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                // 2. 构建 GLM API 请求
                JSONObject requestBody = new JSONObject();
                requestBody.put("model", AiConfig.MODEL);

                JSONObject message = new JSONObject();
                message.put("role", "user");

                // content 数组：图片 + 文本指令
                JSONObject imagePart = new JSONObject();
                imagePart.put("type", "image_url");
                JSONObject urlObj = new JSONObject();
                urlObj.put("url", "data:image/jpeg;base64," + base64Image);
                imagePart.put("image_url", urlObj);

                JSONObject textPart = new JSONObject();
                textPart.put("type", "text");
                textPart.put("text", "请识别这张账单截图，提取金额、商户名称、日期。只返回一个JSON，不要其他文字：{\"amount\":数字,\"merchant\":\"商户名\",\"date\":\"YYYY-MM-DD\"}");

                message.put("content", "[" + imagePart.toString() + "," + textPart.toString() + "]");

                requestBody.put("messages", "[" + message.toString() + "]");

                // 3. 发送 HTTP 请求
                URL url = new URL(AiConfig.API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + AiConfig.API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);

                OutputStream os = conn.getOutputStream();
                os.write(requestBody.toString().getBytes("UTF-8"));
                os.close();

                // 4. 读取响应
                int responseCode = conn.getResponseCode();
                BufferedReader reader;
                if (responseCode >= 200 && responseCode < 300) {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                } else {
                    reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                }
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                if (responseCode != 200) {
                    callback.onError("API 错误 " + responseCode + ": " + response.toString());
                    return;
                }

                // 5. 解析返回的 JSON
                JSONObject json = new JSONObject(response.toString());
                String content = json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                // 6. 提取 JSON 数据
                // 先找 content 里的 JSON 部分
                int jsonStart = content.indexOf('{');
                int jsonEnd = content.lastIndexOf('}') + 1;
                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    String jsonStr = content.substring(jsonStart, jsonEnd);
                    JSONObject result = new JSONObject(jsonStr);
                    double amount = result.optDouble("amount", 0);
                    String merchant = result.optString("merchant", "");
                    String date = result.optString("date", "");
                    android.util.Log.d("AiService", "识别结果: 金额=" + amount + " 商户=" + merchant + " 日期=" + date);
                    callback.onSuccess(amount, merchant, date);
                } else {
                    callback.onError("无法解析识别结果: " + content);
                }

            } catch (Exception e) {
                android.util.Log.e("AiService", "识别失败", e);
                callback.onError("识别失败: " + e.getMessage());
            }
        });
    }
}
