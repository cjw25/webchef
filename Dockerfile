# 1단계: 빌드 환경
FROM eclipse-temurin:17-jdk-jammy AS build
COPY . .
RUN chmod +x gradlew
RUN ./gradlew build -x test

# 2단계: 실행 환경
FROM eclipse-temurin:17-jdk-jammy
COPY --from=build /build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]