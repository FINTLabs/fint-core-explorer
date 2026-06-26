FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace
COPY . .
RUN chmod +x gradlew && ./gradlew --no-daemon clean bootJar

FROM eclipse-temurin:25-jre
ENV JAVA_TOOL_OPTIONS="-XX:+ExitOnOutOfMemoryError"
COPY --from=builder /workspace/build/libs/fint-core-explorer-*.jar /data/fint-core-explorer.jar
ENTRYPOINT ["java", "-jar", "/data/fint-core-explorer.jar"]
