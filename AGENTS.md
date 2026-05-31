# OpenCode Agent Instructions

## Language
- **必須使用繁體中文**與使用者進行溝通、撰寫說明與註解 (MUST use Traditional Chinese for communication and documentation).
## Build & Run Environment
- **必須詢問當前使用的java路徑 並儲存到環境變數 除非.env java-now-home有 把拿到的路徑暫時用到當前環境變數**
- **環境設定讀取優先權 (重要)**：
  - 在執行任何啟動或測試指令前，請先檢查當前專案目錄下是否存在 **`.run` 資料夾**。
  - **若存在 `.run` 目錄，必須優先讀取其中的配置檔案 (如 XML)**，並將其內嵌的環境變數、Spring Profiles 或啟動參數作為最高優先級應用於後續的執行環境中。
  - 若不存在，則退回參考標準的 `.env.example` 進行設定。若在 Docker 內執行後端，需設定 `APP_IN_DOCKER=true`。
- **Infrastructure**: 必須先執行 `docker compose -f compose.yaml up -d` 啟動基礎服務 (PostgreSQL, Redis, Kafka, Zookeeper)。
- **Local Dev Server**: 使用 `./mvnw spring-boot:run` 啟動，預設運行於 port `8000`。

## Testing & Quality

- **Test Command**: 專案標準指令為 `./mvnw test`。測試預設使用 H2 in-memory database。
  - **【Token 節省黑魔法 (Windows/CLI 必讀)】**：
    為了讓底層的 RTK 盾牌能完美看穿 Maven 繁重的日誌包裝，並精簡 JUnit 的重複輸出以節省 90% 以上的 Token：
    1. 在調用終端機執行測試時，**禁止**直接下達 `./mvnw test`。
    2. **必須優先使用以下原生指令包裹**：`rtk ./mvnw test`。
    3. 如果專案內包含 Surefire 外掛，可進一步使用 `rtk ./mvnw test -Dsurefire.useFile=false` 迫使日誌輸出至主終端機，讓
       RTK 進行極致壓縮。
  - **Coverage**: `./mvnw jacoco:report`。Jacoco 覆蓋率報告位於 `target/site/jacoco/index.html`。
  - **Coverage Rules**: 專案設定了最低 80% 的覆蓋率要求 (`BUNDLE` 級別)。注意：多數的對外介面與資料存取層 (Controller,
    Entity, Dto, mapper 等) 在 `pom.xml` 中被設定排除覆蓋率計算。
  - **Mockito Warning**: 測試已在 Maven Surefire 中設定 `-XX:+EnableDynamicAgentLoading` 來消除 Java 21 下的警告。

## Architecture & Code Conventions
- **Base Package**: `com.example.BackendApi` (注意大小寫)
- **Generators**: 專案大量使用 MapStruct 與 Lombok，Maven 已設定對應的 Annotation Processors。
- **Package Quirks**: 請遵守現有的 Package 命名與大小寫慣例：
  - 首字母大寫: `Aop`, `Dto`, `Entity`, `Repository`, `Service`, `Timer`, `Util`, `WebSocket`,`Annotation`, `Config`, `Controller`, `Dataaccess`, `Exception`, `Filter`, `Mapper`
