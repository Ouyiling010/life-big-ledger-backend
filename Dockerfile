# 第一阶段：用 Maven 构建 jar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 第二阶段：用 JDK 21 运行 jar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 7860

# 👇 核心就是下面这一行，注意里面的 "-Dserver.port=7860"
ENTRYPOINT ["java", "-jar", "-Dserver.port=7860", "app.jar"]