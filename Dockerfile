FROM eclipse-temurin:17-jdk-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 필요한 패키지 설치: unzip, curl, tini
RUN apk add --no-cache unzip curl tini

# tini를 ENTRYPOINT로 설정
ENTRYPOINT ["/sbin/tini", "--"]

# JAR 복사
COPY ./build/libs/*SNAPSHOT.jar project.jar

# promtail 다운로드 및 설정 (✅ zip 다운로드 → unzip → mv → chmod)
RUN curl -L -o promtail.zip https://github.com/grafana/loki/releases/download/v2.9.2/promtail-linux-amd64.zip && \
    unzip promtail.zip && \
    mv promtail-linux-amd64 promtail && \
    chmod +x promtail && \
    rm promtail.zip

# promtail 설정파일 복사
COPY promtail-config.yaml /app/promtail-config.yaml

# 로그 디렉토리 생성
RUN mkdir -p /app/logs

# Spring Boot + Promtail 동시 실행 → 하나 죽으면 컨테이너 종료
CMD sh -c "java -jar project.jar > /app/logs/app.log 2>&1 & ./promtail -config.file=/app/promtail-config.yaml & wait -n"