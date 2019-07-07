FROM openjdk:8-jdk
WORKDIR /opt/docker
ADD --chown=daemon:daemon opt /opt
USER daemon
ENTRYPOINT bin/csc536-final-project -Dakka.remote.netty.tcp.hostname="$(eval "echo $AKKA_REMOTING_BIND_HOST")" -Dakka.management.http.hostname="$(eval "echo $AKKA_REMOTING_BIND_HOST")"
CMD []
USER root
