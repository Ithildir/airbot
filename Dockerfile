FROM openjdk:8
MAINTAINER Andrea Di Giorgi <adigiorgi@outlook.com>

COPY . /opt/airbot
WORKDIR /opt/airbot
EXPOSE 8080
ENTRYPOINT ["./gradlew", "run", "--no-daemon"]