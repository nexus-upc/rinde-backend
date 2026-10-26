FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY --chmod=755 mvnw ./mvnw
COPY gateway/pom.xml gateway/pom.xml
COPY gateway/src/ gateway/src/
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -ntp -f gateway/pom.xml package -DskipTests \
    && cp gateway/target/rinde-gateway-*.jar /tmp/application.jar

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app
COPY --from=build --chown=10001:10001 /tmp/application.jar ./application.jar
USER 10001:10001
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
