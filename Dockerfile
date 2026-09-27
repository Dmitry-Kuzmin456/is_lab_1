FROM quay.io/wildfly/wildfly:31.0.1.Final-jdk17

ADD https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar /tmp/postgresql.jar
COPY docker/wildfly.cli /tmp/wildfly.cli

USER root
RUN chown jboss:jboss /tmp/postgresql.jar /tmp/wildfly.cli
USER jboss

RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/wildfly.cli
