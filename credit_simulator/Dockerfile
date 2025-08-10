FROM openjdk:17-jdk-slim

# Install fonts and dependencies for Apache POI autoSizeColumn
RUN apt-get update && apt-get install -y \
    fontconfig \
    libfreetype6 \
    libfreetype6-dev \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy project
COPY . .

# Copy sample file input into container
COPY file_inputs.txt /app/file_inputs.txt

# Build project
RUN ./mvnw clean package -DskipTests

# Set entrypoint and default command
ENTRYPOINT ["java", "-jar", "target/credit_simulator-0.0.1-SNAPSHOT.jar"]
CMD []