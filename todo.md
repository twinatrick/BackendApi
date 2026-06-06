
### 實作計畫 - 職缺爬取與 Gemini API 整合

#### **1. 資料庫結構與實體 (Entity)**

*   **Company (公司)**
    *   `id`: Primary Key (UUID 或 Long)
    *   `name`: 公司名稱 (String)
    *   `website`: 公司官網 (用於爬取職缺, String)
    *   `description`: 公司簡介 (可由 Gemini 整理後更新, Text)
    *   `createdAt`: 創建時間 (LocalDateTime)
    *   `updatedAt`: 更新時間 (LocalDateTime)
*   **JobPosting (職缺)**
    *   `id`: Primary Key (UUID 或 Long)
    *   `companyId`: 外鍵，關聯到 `Company` (Long/UUID)
    *   `title`: 職缺標題 (String)
    *   `url`: 職缺連結 (String)
    *   `description`: 職缺內容 (原始或由 Gemini 整理, Text)
    *   `requirements`: 職位要求 (由 Gemini 提取, Text)
    *   `responsibilities`: 職位職責 (由 Gemini 提取, Text)
    *   `salaryRange`: 薪資範圍 (由 Gemini 提取, String)
    *   `postedDate`: 發布日期 (LocalDate)
    *   `geminiAnalysis`: Gemini 對職缺的分析結果 (例如，推薦指數、匹配度等, Text/JSONB)
    *   `createdAt`: 創建時間 (LocalDateTime)
    *   `updatedAt`: 更新時間 (LocalDateTime)
*   **UserJobLink (使用者與職缺連結)**
    *   `id`: Primary Key (UUID 或 Long)
    *   `userId`: 外鍵，關聯到 `User` (已存在) (Long)
    *   `jobPostingId`: 外鍵，關聯到 `JobPosting` (Long/UUID)
    *   `userNotes`: 使用者對職缺的筆記 (Text)
    *   `geminiFeedback`: Gemini 根據使用者資訊對職缺提供的分析回饋 (Text/JSONB)
    *   `createdAt`: 創建時間 (LocalDateTime)
    *   `updatedAt`: 更新時間 (LocalDateTime)

#### **2. 公司資訊管理 (CRUD)**

*   **Entity**: 新增 `src/main/java/com/example/BackendApi/Entity/Company.java`。
*   **Repository**: 新增 `src/main/java/com/example/BackendApi/Repository/CompanyRepository.java` (繼承 `JpaRepository`)。
*   **DataAccess**: 新增 `src/main/java/com/example/BackendApi/DataAccess/ICompanyDataAccess.java` 和 `src/main/java/com/example/BackendApi/DataAccess/impl/CompanyDataAccessImpl.java`。
*   **Service**: 新增 `src/main/java/com/example/BackendApi/Service/ICompanyService.java` 和 `src/main/java/com/example/BackendApi/Service/impl/CompanyService.java`，提供公司 CRUD 邏輯。
*   **Controller**: 新增 `src/main/java/com/example/BackendApi/Controller/CompanyController.java`，提供公司相關 API 接口。
*   **DTO**: 新增 `src/main/java/com/example/BackendApi/Dto/Vo/CompanyVo.java`、`src/main/java/com/example/BackendApi/Dto/Vo/CreateCompanyRequest.java`、`src/main/java/com/example/BackendApi/Dto/Vo/UpdateCompanyRequest.java` 等。
*   **Mapper**: 新增 `src/main/java/com/example/BackendApi/Mapper/CompanyMapper.java` (使用 MapStruct)。
*   **Swagger/OpenAPI**: 為 `CompanyController` 添加 API 文件註解。
*   **權限管理**: 考慮新增 `RequirePermission` 註解來保護公司相關 API。

#### **3. 職缺爬取與 Gemini API 整合**

*   **Entity**: 新增 `src/main/java/com/example/BackendApi/Entity/JobPosting.java`。
*   **Repository**: 新增 `src/main/java/com/example/BackendApi/Repository/JobPostingRepository.java`。
*   **DataAccess**: 新增 `src/main/java/com/example/BackendApi/DataAccess/IJobPostingDataAccess.java` 和 `src/main/java/com/example/BackendApi/DataAccess/impl/JobPostingDataAccessImpl.java`。
*   **Service**: 新增 `src/main/java/com/example/BackendApi/Service/IJobPostingService.java` 和 `src/main/java/com/example/BackendApi/Service/impl/JobPostingService.java`。
    *   包含根據 `Company` 資訊呼叫網頁爬取職缺的邏輯。
    *   整合 Gemini API，將爬取到的原始職缺內容傳送給 Gemini 進行分析和整理。
    *   將 Gemini 返回的分析結果儲存到 `JobPosting` 的 `geminiAnalysis` 字段。
*   **Controller**: 新增 `src/main/java/com/example/BackendApi/Controller/JobPostingController.java`，提供職缺查詢和觸發職缺爬取的 API。
*   **Web Crawler 模組 (彈性化策略)**:
    *   新增 `src/main/java/com/example/BackendApi/Crawler/JobCrawler.java` 介面，定義爬蟲行為。
    *   實作 `src/main/java/com/example/BackendApi/Crawler/impl/WebCollectorJobCrawler.java` (基於 WebCollector/Jsoup)。
    *   實作 `src/main/java/com/example/BackendApi/Crawler/impl/SeleniumJobCrawler.java` (基於 Selenium，處理動態頁面)。
    *   設計工廠模式或策略模式來選擇合適的爬蟲實作。
    *   考慮進階反爬技術：代理 IP 池、User-Agent 隨機化、增加請求延遲、處理驗證碼 (可選)。
*   **Gemini API 客戶端**:
    *   新增 `src/main/java/com/example/BackendApi/Service/GeminiApiClient.java` 服務類，負責與 Gemini API 進行互動。
    *   利用 Spring AI 的功能來更方便地整合 Gemini API。
    *   處理 API 請求和回應的資料轉換。
*   **排程任務 (Optional)**:
    *   可以使用 Spring 的 `@Scheduled` 註解，定期觸發職缺爬取和更新。

#### **4. 使用者與職缺連結、Gemini 分析**

*   **Entity**: 新增 `src/main/java/com/example/BackendApi/Entity/UserJobLink.java`。
*   **Repository**: 新增 `src/main/java/com/example/BackendApi/Repository/UserJobLinkRepository.java`。
*   **DataAccess**: 新增 `src/main/java/com/example/BackendApi/DataAccess/IUserJobLinkDataAccess.java` 和 `src/main/java/com/example/BackendApi/DataAccess/impl/UserJobLinkDataAccessImpl.java`。
*   **Service**: 新增 `src/main/java/com/example/BackendApi/Service/IUserJobLinkService.java` 和 `src/main/java/com/example/BackendApi/Service/impl/UserJobLinkService.java`。
    *   提供使用者將職缺加入收藏或追蹤的功能。
    *   當使用者與職缺連結時，觸發 Gemini 進行更深度的分析。
    *   將 Gemini 返回的分析結果儲存到 `UserJobLink` 的 `geminiFeedback` 字段。
*   **Controller**: 新增 `src/main/java/com/example/BackendApi/Controller/UserJobLinkController.java`，提供使用者管理職缺連結的 API。
*   **DTO**: 新增 `src/main/java/com/example/BackendApi/Dto/Vo/CreateUserJobLinkRequest.java` 等。

#### **5. 環境設定與基礎設施**

*   **`pom.xml`**:
    *   新增必要的 Maven 依賴：
        *   `org.springframework.boot:spring-boot-starter-web` (如果 `CompanyController` 需要)
        *   `org.springframework.boot:spring-boot-starter-data-jpa`
        *   `org.postgresql:postgresql` Driver
        *   `org.mapstruct:mapstruct`
        *   `org.projectlombok:lombok` (確保已配置)
        *   `cn.edu.hfut.dmic.webcollector:WebCollector` (或其最新版本)
        *   `org.seleniumhq.selenium:selenium-java`
        *   `io.github.bonigarcia:webdrivermanager` (方便管理瀏覽器驅動)
        *   `org.springframework.ai:spring-ai-vertexai-gemini-spring-boot-starter` (或 Spring AI 提供的 Gemini 整合 starter)
    *   更新 `maven-compiler-plugin` 以確保 MapStruct 生效。
*   **`application.yml` 或 `application.properties`**:
    *   配置新的資料庫表映射 (如果需要)。
    *   配置 Gemini API 的相關設定 (API Key, Endpoint 等)。
    *   配置 Selenium 相關設定 (如 `webdriver.chrome.driver` 路徑，headless 模式開關等)。
*   **Docker Compose (`compose.yaml`)**:
    *   目前已包含 PostgreSQL，如果需要其他數據儲存或服務，可能需要更新。

#### **6. 測試**

*   為每個 Service 和 Controller 撰寫單元測試和整合測試，確保功能正確性。
*   針對爬蟲模組，需要撰寫模擬不同網站結構的測試案例。
*   Gemini API 整合部分，需要考慮 mock 外部 API 呼叫，以確保測試的穩定性和速度。

#### **7. 安全性考量**

*   **API 金鑰安全**: Gemini API 金鑰應妥善保管，使用環境變數或 Spring Cloud Config 等方式管理，避免硬編碼。
*   **網頁爬取合法性**: 確保爬取行為遵守網站的 `robots.txt` 協定，並考慮對目標網站的請求頻率進行限制，避免被封鎖或造成服務負擔。
*   **資料隱私**: 處理使用者資料時，應遵守相關的隱私法規。
*   **輸入驗證**: 對所有使用者輸入進行嚴格驗證，防止 SQL 注入、XSS 等安全漏洞。
