FROM openjdk:11
WORKDIR /app/
ADD /build/libs/gateway-0.0.1-SNAPSHOT.jar gateway.jar
EXPOSE 80
ENTRYPOINT ["java","-jar","gateway.jar","--debug"]