FROM maven:3-eclipse-temurin-26 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:resolve -B -q
COPY src src
RUN mvn clean package -DskipTests -B -q

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN adduser --system --group appuser
USER appuser
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=UTC", "-jar", "app.jar"]
