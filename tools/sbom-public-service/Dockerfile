# Stage 1: Build and run the Java application with Maven and Java 17
FROM maven:3.9-eclipse-temurin-17

WORKDIR /app

# Copy Maven configuration first for better dependency-layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Create the SBOM upload directory
RUN mkdir -p /data/sbom

# Configure the SBOM upload path
ENV SBOM_UPLOAD_PATH=/data/sbom

EXPOSE 9051

# Run the built Spring Boot application
CMD ["sh", "-c", "java -jar target/sbom-utils-service-*.jar --sbom.upload.path=${SBOM_UPLOAD_PATH}"]