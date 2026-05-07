# OpenCode Agent Instructions

## Language
- **必須使用繁體中文**與使用者進行溝通、撰寫說明與註解 (MUST use Traditional Chinese for communication and documentation).

## Build & Run Environment
- **Infrastructure**: 必須先執行 `docker compose -f compose.yaml up -d` 啟動基礎服務 (PostgreSQL, Redis, Kafka, Zookeeper)。
- **Local Dev Server**: 使用 `./mvnw spring-boot:run` 啟動，預設運行於 port `8000`。
- **Environment**: 參考 `.env.example`。若在 Docker 內執行後端，需設定 `APP_IN_DOCKER=true`。

## Testing & Quality
- **Test Command**: `./mvnw test`。測試預設使用 H2 in-memory database。
- **Coverage**: `./mvnw jacoco:report`。Jacoco 覆蓋率報告位於 `target/site/jacoco/index.html`。
- **Coverage Rules**: 專案設定了最低 80% 的覆蓋率要求 (`BUNDLE` 級別)。注意：多數的對外介面與資料存取層 (Controller, Entity, Dto, mapper 等) 在 `pom.xml` 中被設定排除覆蓋率計算。
- **Mockito Warning**: 測試已在 Maven Surefire 中設定 `-XX:+EnableDynamicAgentLoading` 來消除 Java 21 下的警告。

## Architecture & Code Conventions
- **Base Package**: `com.example.backendApi` (注意大小寫)
- **Generators**: 專案大量使用 MapStruct 與 Lombok，Maven 已設定對應的 Annotation Processors。
- **Package Quirks**: 請遵守現有的 Package 命名與大小寫慣例：
  - 首字母大寫: `Aop`, `Dto`, `Entity`, `Repository`, `Service`, `Timer`, `Util`, `WebSocket`
  - 首字母小寫: `annotation`, `config`, `controller`, `dataaccess`, `exception`, `filter`, `mapper`
