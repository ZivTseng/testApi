Blog System API (Spring Boot 3 + JWT)
這是一個基於 Spring Boot 3 開發的部落格系統後端 API，整合了使用者驗證與文章管理功能。

技術棧
Java 17

Spring Boot 3.4.x

Spring Security & JWT (安全驗證)

Spring Data JPA (資料庫存取)

H2 Database (開發用記憶體資料庫)

Lombok (簡化程式碼)

SpringDoc OpenAPI (Swagger UI) (API 文件)

核心功能
使用者模組：支援 JWT 登入驗證、登入防呆（啟動時自動檢查測試帳號）。

會員註冊

文章管理 (CRUD)：

建立文章（自動關聯當前登入使用者）。

支援分頁查詢與標題關鍵字搜尋。

文章更新與刪除。

安全性設計：

除了登入與 Swagger 路徑外，其餘 API 均受 JWT 保護。

全域例外處理 (Global Exception Handler)，統一錯誤回傳格式。

文件化：整合 Swagger UI，支援在網頁直接進行 JWT 授權測試。

快速啟動
複製專案後，直接使用 IntelliJ IDEA 開啟。

執行 TestApiApplication.java。

系統啟動時會自動建立一個測試帳號：

Email: admin@test.com

Password: 123456

如何測試
訪問 API 文件：http://localhost:8080/swagger-ui/index.html

呼叫 /api/auth/login 取得 Token。

點擊右上角 Authorize，貼入 Token。

開始測試 /api/articles 相關介面。