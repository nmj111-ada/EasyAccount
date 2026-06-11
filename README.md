# EasyAccount 记账 App

学生自学开发的记账应用，用于个人财务管理。

## 技术栈

| 端 | 技术 |
|---|---|
| Android App | Java + Room + MPAndroidChart + MVVM |
| 后端 | Spring Boot 3.2 + JPA + MySQL |
| Web 前端 | React + TypeScript + Recharts + Vite |

## 功能

- 📝 记账（42个二级分类 + 7大类统计）
- 📊 环形饼图 + 柱状图统计
- 🔍 关键词/分类/日期搜索
- 💰 月度预算管理
- 🔁 周期记账（WorkManager 自动生成）
- 🌙 深色模式
- ☁️ 云同步
- 🤖 AI 拍照记账（智谱 GLM-4V-Flash）
- 🌐 Web 管理端

## 项目结构

```
D:\
├── account_app\        Android App
├── account_server\     Spring Boot 后端
└── account_web\        React Web 前端
```
