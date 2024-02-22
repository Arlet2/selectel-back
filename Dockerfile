FROM gradle:8.6.0-jdk21 AS build

COPY . .
RUN gradle bootJar

FROM openjdk:21

COPY --from=build /build/libs/selectel-back-0.0.1-rolling.jar /

CMD java -jar selectel-back-0.0.1-rolling.jar