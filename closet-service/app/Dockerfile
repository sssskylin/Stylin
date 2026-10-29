FROM gradle:9.1.0-jdk25 AS build

WORKDIR /src

COPY . .

COPY gradle /gradle

# Даем права на выполнение wrapper'у
RUN chmod +x ./gradlew

# Запускаем сборку с указанием папки проекта
RUN ./gradlew -p closet-service :app:installDist --no-daemon


FROM eclipse-temurin:25-jre

WORKDIR /opt/app

COPY --from=build /src/app/build/install/app/ ./

EXPOSE 8080

CMD ["bin/app"]