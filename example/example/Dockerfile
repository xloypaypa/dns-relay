FROM openjdk:11

ADD ./certificateChecker.clj /
ADD ./config.clj /
ADD ./dns-relay-1.0.0.jar /

CMD ["java","-jar","dns-relay-1.0.0.jar"]