FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY lib ./lib
COPY src ./src
COPY web ./web
COPY data ./data

RUN mkdir -p out \
    && find src -name "*.java" -print0 | xargs -0 javac --add-modules jdk.httpserver -d out

EXPOSE 10000

CMD ["java", "--add-modules", "jdk.httpserver", "-cp", "out:lib/*", "farmmarket.web.MarketplaceWebServer"]
