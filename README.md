# Java 21 Spring Boot 常見技術 實作方法

通用後端範例專案，整合使用者/角色/權限、專案/技能管理、資料查詢與告警設定等常見後端需求，並提供 REST API、WebSocket 與 Kafka Consumer。

## 技術棧

- Java 21
- Spring Boot 3.4.2
- Spring Web / Spring Data JPA / MyBatis
- PostgreSQL / Redis
- Kafka + Zookeeper
- WebSocket
- Springdoc OpenAPI (Swagger UI)
- JUnit 5 / Mockito / H2 (測試)

## 提供的介面類型

- REST API
- WebSocket
- Kafka Consumer

## 啟動方式

### Docker Compose

1. 啟動基礎服務（PostgreSQL、Redis、Kafka、Zookeeper）

```bash
docker compose -f compose.yaml up -d
```

2. 本機啟動後端（見下方）

### 本機啟動

```bash
./mvnw spring-boot:run
```

## 重要設定

- 服務埠：`8000`
- JWT Secret：`jwt.secret.use`
- PostgreSQL：`localhost:5432`
- Redis：`localhost:6379`
- Kafka：`localhost:9092`

可在 `compose.yaml` 查看各服務連線設定。

## Swagger

- `http://localhost:8000/swagger-ui/index.html`

## 測試與覆蓋率

```bash
./mvnw test
./mvnw jacoco:report
```

覆蓋率報告位置：`target/site/jacoco/index.html`
