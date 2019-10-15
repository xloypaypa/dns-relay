FROM gradle:jdk11 AS builder

ADD ./ /code
RUN cd /code && gradle clean build --info

FROM openjdk:11

COPY --from=builder /code/build/libs/dns-relay-1.0.0.jar /

CMD ["java","-jar","dns-relay-1.0.0.jar"]