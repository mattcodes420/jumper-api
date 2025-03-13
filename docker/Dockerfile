# Stage 1: Build the Java API
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Create the final image with both Java and Python
FROM openjdk:17-slim

# Install Python and timezone utilities
RUN apt-get update && \
    apt-get install -y python3 python3-pip tzdata && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set timezone to PST
ENV TZ=America/Los_Angeles
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Create shared data directory
RUN mkdir -p /app/data

# Copy the Java API
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Copy the Python script and its requirements
WORKDIR /app/scripts
COPY scripts/requirements.txt .
RUN pip3 install --no-cache-dir -r requirements.txt
COPY scripts/ .

# Copy the start script
WORKDIR /app
COPY docker/start.sh .
RUN chmod +x start.sh

EXPOSE 8080
CMD ["./start.sh"]