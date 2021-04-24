FROM adoptopenjdk/openjdk11:alpine as builder
RUN apk add --no-cache maven
ADD . /src
WORKDIR /src
RUN mvn package -Dmaven.test.skip=true

FROM adoptopenjdk/openjdk11:alpine-jre
COPY --from=builder /src/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]