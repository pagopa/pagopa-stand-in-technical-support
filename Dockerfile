#
# Build
#
FROM maven:3.9.5-amazoncorretto-17-al2023@sha256:eeaa7ab572d931f7273fc5cf31429923f172091ae388969e11f42ec6dd817d74 as buildtime
WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests


FROM amazoncorretto:17.0.9-alpine3.18@sha256:df48bf2e183230040890460ddb4359a10aa6c7aad24bd88899482c52053c7e17 as builder
COPY --from=buildtime /build/target/*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract


FROM ghcr.io/pagopa/docker-base-springboot-openjdk17:v1.1.2@sha256:de4e831fce6db6676c06ed16565ff19d4c86c929218eec228fae758bbb129f78
ADD --chown=spring:spring https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.32.1/opentelemetry-javaagent.jar .

COPY --chown=spring:spring  --from=builder dependencies/ ./
COPY --chown=spring:spring  --from=builder snapshot-dependencies/ ./
# https://github.com/moby/moby/issues/37965#issuecomment-426853382
RUN true
COPY --chown=spring:spring  --from=builder spring-boot-loader/ ./
COPY --chown=spring:spring  --from=builder application/ ./

EXPOSE 8080

ENTRYPOINT ["java","-javaagent:opentelemetry-javaagent.jar","--enable-preview","org.springframework.boot.loader.JarLauncher"]
