ARG GLOBAL_BUILD_BASEIMAGE=image.cestc.cn/release/cclinux2209:22.09.2-master-15-1

FROM ${GLOBAL_BUILD_BASEIMAGE}
MAINTAINER "jinmancang <jinmancang@cestc.cn>"
RUN dnf install -y --nodocs java-11-openjdk socat  nc  net-tools\
    && dnf clean all
ADD target/kafka-agent-1.0.jar /cmq/kafka-agent.jar
EXPOSE 8080
ENV LANG C.UTF-8
WORKDIR /cmq/
RUN chmod 777 -R /cmq
CMD ["/bin/sh", "-c", "java -Dfile.encoding=UTF-8 -jar $JAVA_OPTS kafka-agent.jar"]