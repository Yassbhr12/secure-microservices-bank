FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

COPY pom.xml .

RUN mvn -B -ntp dependency:go-offline

COPY src ./src

RUN mvn -B -ntp clean package -DskipTests


FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S spring \
    && adduser -S spring -G spring \
    && mkdir -p /app \
    && chown -R spring:spring /app

WORKDIR /app

COPY --from=builder --chown=spring:spring \
    /build/target/*.jar app.jar

USER spring:spring

EXPOSE 8083

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
