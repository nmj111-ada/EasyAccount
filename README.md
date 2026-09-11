# EasyAccount

EasyAccount 是一个个人记账项目，包含 Android 移动端、Web 管理端和 Spring Boot 数据同步服务，支持账目记录、分类统计、预算、周期记账和云同步。

## 模块

| 模块 | 目录 | 技术 | 说明 |
| --- | --- | --- | --- |
| Android App | `app/` | Java, AndroidX, Room, MVVM | 本地记账与同步客户端 |
| Web 管理端 | `web/` | React 18, TypeScript, Vite, Recharts | 浏览器账目与统计查看 |
| 服务端 | `server/` | Spring Boot 3.2, JPA, MySQL | 数据同步和查询 API |

## 项目结构

```text
EasyAccount/
├── app/                 Android 应用
├── web/                 React Web 管理端
├── server/              Spring Boot 服务端
├── README.md
└── .gitignore
```

## 前置条件

- JDK 17
- Maven 3.9 或更高版本
- Node.js 18 或更高版本和 npm
- MySQL 8
- Android Studio（构建 Android 应用时需要）

## 启动服务端

先确保 MySQL 可用。服务端默认会创建 `easy_account` 数据库，并通过环境变量读取连接信息：

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/easy_account?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Asia/Shanghai'
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = '<your-database-password>'
```

```powershell
cd server
mvn spring-boot:run
```

服务默认运行在 `http://localhost:8080`，API 路由以 `/api` 开头。配置示例见 `server/src/main/resources/application.example.yml`。

## 启动 Web 管理端

先启动服务端，再在另一个终端运行：

```powershell
cd web
npm install
npm run dev
```

Vite 会将 `/api` 请求代理至 `http://localhost:8080`。生产构建使用 `npm run build`。

## 运行 Android 应用

1. 使用 Android Studio 打开仓库根目录。
2. 等待 Gradle 同步完成，选择设备或模拟器后运行 `app`。
3. 真机同步时使用电脑的局域网 IP 或部署地址，不要使用 `localhost`。


## 常用验证命令

```powershell
cd web
npm run build

cd ../server
mvn test package
```
