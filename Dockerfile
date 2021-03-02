FROM gradle:6.8.2-jdk11 as builder
USER root
COPY . .
RUN gradle --no-daemon build

FROM gcr.io/distroless/java:11
ENV JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError -Dhttps.protocols=TLSv1.2
COPY --from=builder /home/gradle/build/libs/fint-core-exporter-*.jar /data/fint-core-exporter.jar
CMD ["/data/fint-core-exporter.jar"]