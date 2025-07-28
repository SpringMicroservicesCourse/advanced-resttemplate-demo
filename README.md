# Advanced RestTemplate Demo ⚡

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 專案介紹

本專案示範如何在 Spring Boot 專案中，進階自訂 RestTemplate 的底層 HTTP 連線池、Keep-Alive 策略與超時設定，並結合台灣常用專業術語與清楚註解，方便團隊理解與維護。

- **核心功能**：自訂 Apache HttpClient5 連線池、Keep-Alive、timeout，並整合於 Spring Boot RestTemplate。
- **解決問題**：解決高併發下 HTTP 連線資源耗盡、連線存活策略不明確、timeout 未設導致系統 hang 死等問題。
- **主要特色**：程式碼註解清楚、符合台灣團隊溝通習慣、易於擴充與維護。
- **目標使用者**：Java/Spring 團隊、需自訂 HTTP 連線池與效能優化的後端工程師。

> 💡 **為什麼選擇此專案？**
> - 範例完整，涵蓋連線池、Keep-Alive、timeout、序列化等常見實務需求
> - 註解清楚，方便新手與團隊成員快速上手
> - 完全採用台灣常用專業用語，溝通無障礙

### 🎯 專案特色

- 支援自訂 HttpClient5 連線池與 Keep-Alive 策略
- 重要程式區塊皆有詳細中文註解
- 依照台灣軟體團隊習慣設計，易於維護與擴充

## 技術棧

### 核心框架
- **Spring Boot 3.2.5** - 主流 Java 微服務框架，快速建構 RESTful API
- **Apache HttpClient5** - 高效能 HTTP 連線池與客製化連線管理

### 開發工具與輔助
- **Maven** - 專案建置與依賴管理
- **Lombok** - 精簡 Java POJO 寫法
- **Joda-Money** - 處理金額型別

## 專案結構

```
advanced-resttemplate-demo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── tw/fengqing/spring/springbucks/customer/
│   │   │       ├── model/
│   │   │       ├── support/
│   │   │       └── CustomerServiceApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
└── README.md
```

## 快速開始

### 前置需求
- Java 21 (建議使用 OpenJDK 21)
- Maven 3.8+

### 安裝與執行

1. **克隆此倉庫：**
   ```bash
   git clone https://github.com/SpringMicroservicesCourse/advanced-resttemplate-demo.git
   ```

2. **進入專案目錄：**
   ```bash
   cd advanced-resttemplate-demo
   ```

3. **編譯專案：**
   ```bash
   mvn compile
   ```

4. **執行應用程式：**
   ```bash
   mvn spring-boot:run
   ```

## 進階說明

### 環境變數
```properties
# 必要設定（如有資料庫或外部 API 可依需求補充）
API_KEY=your-api-key

# 選用設定
LOG_LEVEL=INFO
```

### 設定檔說明
```properties
# application.properties 主要設定
# 依實際需求補充
```

## 參考資源

- [Spring Boot 官方文件](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Apache HttpClient5 官方文件](https://hc.apache.org/httpcomponents-client-5.3.x/index.html)
- [Spring 官方 RestTemplate 文件](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html)

## 注意事項與最佳實踐

### ⚠️ 重要提醒

| 項目   | 說明             | 建議做法         |
|--------|------------------|------------------|
| 註解   | 重要邏輯區塊需加註解 | 用台灣常用術語   |
| 效能   | HTTP 連線池管理   | 使用 PoolingHttpClientConnectionManager |
| 超時   | 請務必設定 timeout | 避免系統 hang 死 |
| 安全性 | 機敏資訊管理       | 使用環境變數     |

### 🔒 最佳實踐指南

- 重要程式區塊務必加上清楚註解
- 連線池與 Keep-Alive 策略請依實際流量調整參數
- timeout 設定不可省略，保護系統資源
- 依團隊習慣統一專業用語，減少溝通誤會

## 授權說明

本專案採用 MIT 授權條款，詳見 LICENSE 檔案。

## 關於我們

我們專注於敏捷專案管理、物聯網（IoT）應用開發與領域驅動設計（DDD），致力於將先進技術與實務經驗結合，打造好用又靈活的軟體解決方案。

## 聯繫我們

- **FB 粉絲頁**：[風清雲談 | Facebook](https://www.facebook.com/profile.php?id=61576838896062)
- **LinkedIn**：[linkedin.com/in/chu-kuo-lung](https://www.linkedin.com/in/chu-kuo-lung)
- **YouTube 頻道**：[雲談風清 - YouTube](https://www.youtube.com/channel/UCXDqLTdCMiCJ1j8xGRfwEig)
- **風清雲談 部落格**：[風清雲談](https://blog.fengqing.tw/)
- **電子郵件**：[fengqing.tw@gmail.com](mailto:fengqing.tw@gmail.com)

---

**📅 最後更新：[2025-07-25]**  

---