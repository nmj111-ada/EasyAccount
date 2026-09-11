package com.easyaccount.app.ai;

/**
 * AI 配置
 * ⚠️ 在下面的 API_KEY 填入你的智谱开放平台 key
 * 申请地址：https://open.bigmodel.cn/usercenter/apikeys
 */
public class AiConfig {
    public static final String API_KEY = "YOUR_ZHIPU_API_KEY_HERE";

    // GLM-4V-Flash 模型名称（免费）
    public static final String MODEL = "glm-4v-flash";

    // API 端点
    public static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
}
