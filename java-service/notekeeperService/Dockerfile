FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY . .

RUN chmod +x mvnw \
 && ./mvnw -B clean package -DskipTests

EXPOSE 8080

CMD java -jar target/*.jar
