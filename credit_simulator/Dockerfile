FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Build project (pakai Maven)
RUN ./mvnw clean package -DskipTests

# Jalankan aplikasi
CMD ["java", "-jar", "target/credit_simulator-0.0.1-SNAPSHOT.jar"]