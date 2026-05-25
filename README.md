# Java 21 Spring Boot 常見技術 實作方法

通用後端範例專案，整合使用者/角色/權限、專案/技能管理、資料查詢與告警設定等常見後端需求，並提供 REST API、WebSocket 與 Kafka Consumer，且已實作註解式角色權限控管機制。

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

2. 可選：先複製環境變數模板再調整

```bash
cp .env.example .env
```

3. 本機啟動後端（見下方）

### 本機啟動

```bash
./mvnw spring-boot:run
```

### Docker 內啟動後端

若後端服務跑在 Docker 內，請設定 `APP_IN_DOCKER=true`。
當 `APP_IN_DOCKER=true` 且未手動指定 `KAFKA_BOOTSTRAP_SERVERS` 時，後端會自動使用 `kafka:9092`。
否則（預設）會使用 `localhost:9092`。

Kafka 對外廣播主機可用 `KAFKA_ADVERTISED_HOST` 控制：

- Docker 內互連：`KAFKA_ADVERTISED_HOST=kafka`
- 本機連線：`KAFKA_ADVERTISED_HOST=localhost`

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
