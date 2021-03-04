FROM gradle:6.8.2-jdk8 as builder
USER root
COPY . .
RUN gradle --no-daemon build

FROM gcr.io/distroless/java:8
ENV JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError
COPY --from=builder /home/gradle/build/libs/fint-core-explorer-*.jar /data/fint-core-explorer.jar
CMD ["/data/fint-core-explorer.jar"]