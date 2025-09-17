# Stage 1: Build Java app with Maven
FROM maven:3.8.6-openjdk-8

WORKDIR /app

# Copy pom.xml and download dependencies first (better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build JAR
COPY src ./src
RUN mvn clean install -DskipTests

# Stage 2: Run Spring Boot app
# FROM openjdk:8-jre-slim

# WORKDIR /app

# Copy JAR from build stage
# COPY --from=build /app/target/sbom-public-*.jar app.jar

# Create upload folder
RUN mkdir -p /data/sbom

# Set the sbom.upload.path property
ENV SBOM_UPLOAD_PATH=/data/sbom

EXPOSE 9051

# ENTRYPOINT ["java", "-jar", "/app/target/sbom-public-*.jar", "--sbom.upload.path=/data/sbom"]
ENTRYPOINT ["mvn", "spring-boot:run"]