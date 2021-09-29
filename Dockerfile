FROM gradle:7.2.0-jdk11 as builder
USER root
COPY . .
RUN gradle --no-daemon build

FROM gcr.io/distroless/java:11
ENV JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError
COPY --from=builder /home/gradle/build/libs/fint-core-explorer-*.jar /data/fint-core-explorer.jar
CMD ["/data/fint-core-explorer.jar"]