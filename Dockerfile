
# 第一階段：使用支援 Java 21 的 Maven 映像來構建應用
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
# 複製 Maven 配置及原始碼
COPY pom.xml .
COPY src ./src
# 進行打包（跳過測試可加 -DskipTests 參數）
RUN mvn clean package -DskipTests

# 第二階段：使用 OpenJDK 21 作為運行時映像
FROM openjdk:21-jdk
WORKDIR /app
# 從 build 階段複製打包好的 jar 檔
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
