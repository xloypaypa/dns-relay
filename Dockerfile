FROM openjdk:11

ADD ./build/libs/dns-relay-1.0.0.jar /

CMD ["java","-jar","dns-relay-1.0.0.jar"]