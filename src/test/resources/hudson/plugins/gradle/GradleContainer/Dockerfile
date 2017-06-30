FROM alpine:3.5

ENV GRADLE_VERSION 2.10
ENV GRADLE_HOME /usr/local/gradle
ENV PATH ${PATH}:${GRADLE_HOME}/bin
ENV GRADLE_USER_HOME /gradle

WORKDIR /usr/local
RUN apk add --no-cache bash
RUN apk --update add openjdk7-jre
RUN wget  http://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip && \
    unzip gradle-${GRADLE_VERSION}-bin.zip && \
    rm -f gradle-${GRADLE_VERSION}-bin.zip && \
    ln -s gradle-${GRADLE_VERSION} gradle

RUN mkdir -p /gradle && mkdir -p /app
VOLUME /gradle /app

WORKDIR /app
EXPOSE 22
# do something to have docker run in the background and wait for ssh connections
CMD ["sleep", "60"]
