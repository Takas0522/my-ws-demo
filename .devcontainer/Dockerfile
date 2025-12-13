FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm

ARG INSTALL_MAVEN="true"
ARG MAVEN_VERSION="3.9.9"

ARG INSTALL_GRADLE="false"
ARG GRADLE_VERSION=""

RUN if [ "${INSTALL_MAVEN}" = "true" ]; then su vscode -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk install maven \"${MAVEN_VERSION}\""; fi \
    && if [ "${INSTALL_GRADLE}" = "true" ]; then su vscode -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk install gradle \"${GRADLE_VERSION}\""; fi

# Install PostgreSQL client
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends postgresql-client \
    && apt-get clean -y && rm -rf /var/lib/apt/lists/*

# Install Payara Micro (for Jakarta EE applications)
ARG PAYARA_VERSION=6.2024.10
RUN mkdir -p /opt && \
    wget -q https://repo1.maven.org/maven2/fish/payara/extras/payara-micro/${PAYARA_VERSION}/payara-micro-${PAYARA_VERSION}.jar \
    -O /opt/payara-micro.jar && \
    chmod 644 /opt/payara-micro.jar

# Java 21 recommended JVM options
ENV JAVA_OPTS="-Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

