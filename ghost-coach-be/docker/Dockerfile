FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle gradle
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src src
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon bootJar -x test

RUN java -Djarmode=tools -jar build/libs/ghost-coach-be.jar extract --layers --launcher --destination extracted


FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

RUN groupadd -r app && useradd -r -g app app \
    && mkdir -p /app/uploads \
    && chown -R app:app /app

COPY --from=builder --chown=app:app /workspace/extracted/dependencies/ ./
COPY --from=builder --chown=app:app /workspace/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=app:app /workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=app:app /workspace/extracted/application/ ./

USER app

EXPOSE 8080 5005

ENV JAVA_OPTS="" \
    DEBUG_ENABLED="false" \
    DEBUG_PORT="5005" \
    DEBUG_SUSPEND="n"

ENTRYPOINT ["sh", "-c", "exec java \
  $( [ \"$DEBUG_ENABLED\" = \"true\" ] && echo \"-agentlib:jdwp=transport=dt_socket,server=y,suspend=$DEBUG_SUSPEND,address=*:$DEBUG_PORT\" ) \
  $JAVA_OPTS \
  org.springframework.boot.loader.launch.JarLauncher"]

HEALTHCHECK --interval=15s --timeout=3s --start-period=30s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1
