FROM openjdk:17-jdk-slim

# Install fonts and dependencies for Apache POI autoSizeColumn
RUN apt-get update && apt-get install -y \
    fontconfig \
    libfreetype6 \
    libfreetype6-dev \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests

CMD ["java", "-jar", "target/credit_simulator-0.0.1-SNAPSHOT.jar"]