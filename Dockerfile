FROM gradle:8.6.0-jdk21 AS build

WORKDIR /app

COPY . .
RUN gradle bootJar

FROM openjdk:21

COPY --from=build /app/build/libs/selectel-back-0.0.1-rolling.jar /
COPY --from=build /app/static/images/ /static/images/

CMD java -jar selectel-back-0.0.1-rolling.jar

VOLUME static