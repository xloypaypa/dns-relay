FROM openjdk:11

ADD chinaIPList /chinaIPList
ADD ./chinaIPChecker.clj /
ADD ./certificateChecker.clj /
ADD ./config.clj /
ADD ./dns-relay-1.0.0.jar /

CMD ["java","-jar","dns-relay-1.0.0.jar"]