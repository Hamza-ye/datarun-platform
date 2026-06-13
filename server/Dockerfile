FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/server
COPY server/pom.xml .
COPY server/.mvn .mvn
COPY server/mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY server/src src
COPY contracts/patterns /workspace/contracts/patterns
COPY contracts/pattern-definition.schema.json /workspace/contracts/pattern-definition.schema.json
COPY contracts/shapes /workspace/contracts/shapes
RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
ARG IMAGE_VERSION=dev
ARG IMAGE_REVISION=unknown
ARG IMAGE_CREATED=unknown

LABEL org.opencontainers.image.title="Datarun Server" \
      org.opencontainers.image.description="Datarun platform server" \
      org.opencontainers.image.source="https://github.com/Hamza-ye/datarun-platform" \
      org.opencontainers.image.version="${IMAGE_VERSION}" \
      org.opencontainers.image.revision="${IMAGE_REVISION}" \
      org.opencontainers.image.created="${IMAGE_CREATED}"

RUN addgroup -S -g 10001 datarun \
    && adduser -S -D -H -u 10001 -G datarun datarun

WORKDIR /app
COPY --from=build /workspace/server/target/*.jar app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
