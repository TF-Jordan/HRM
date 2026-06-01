# syntax=docker/dockerfile:1.7

FROM maven:3.9.14-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -pl RT-comops-bootstrap -am -Dmaven.test.skip=true package

FROM eclipse-temurin:21.0.10_7-jre-noble
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system iwm \
    && useradd --system --gid iwm --create-home --home-dir /home/iwm iwm

COPY --from=build /workspace/RT-comops-bootstrap/target/RT-comops-bootstrap-0.1.0-SNAPSHOT.jar /app/iwm-backend.jar
COPY scripts/docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

RUN chmod +x /usr/local/bin/docker-entrypoint.sh \
    && chown -R iwm:iwm /app

EXPOSE 8080 8081

USER iwm

ENTRYPOINT ["docker-entrypoint.sh"]
