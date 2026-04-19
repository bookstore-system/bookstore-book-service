# ===== BUILD STAGE =====
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

# cache dependencies trước
COPY pom.xml .
RUN mvn -B -q -e -DskipTests dependency:go-offline

# copy source
COPY src ./src

# build
RUN mvn -B -DskipTests package


# ===== RUNTIME STAGE =====
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]