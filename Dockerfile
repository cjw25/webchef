# 베이스 이미지 (JDK 17 기준, 본인 프로젝트 자바 버전에 맞게 수정)
FROM eclipse-temurin:17-jdk-jammy

# jar 파일 복사 (build/libs 폴더에 있는 파일)
COPY build/libs/*.jar app.jar

# 컨테이너 실행 명령어
ENTRYPOINT ["java", "-jar", "/app.jar"]