FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -DskipTests dependency:go-offline
COPY src/ src/
RUN ./mvnw -B clean package

FROM eclipse-temurin:17-jre-jammy
RUN groupadd --system --gid 10001 scriptsign \
    && useradd --system --uid 10001 --gid scriptsign --home-dir /nonexistent --shell /usr/sbin/nologin scriptsign \
    && mkdir /work \
    && chown scriptsign:scriptsign /work
WORKDIR /app
COPY --from=build --chown=scriptsign:scriptsign /build/target/script-sign-*.jar /app/script-sign.jar
USER 10001:10001
EXPOSE 8080
ENV SIGNING_TEMP_DIRECTORY=/work
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-Djava.io.tmpdir=/work", "-jar", "/app/script-sign.jar"]
