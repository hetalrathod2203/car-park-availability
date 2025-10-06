FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy source code and gradle files
COPY . .

# Make gradlew executable and build the application
RUN chmod +x ./gradlew && ./gradlew build -x test

# Copy the built jar
RUN cp build/libs/*.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]