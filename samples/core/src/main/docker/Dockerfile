# Dockerfile used by alexec/docker-maven-plugin
FROM java:openjdk-7-jre

EXPOSE 8080

ADD oasp4j-sample-core-dev-SNAPSHOT.jar app.jar

RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]