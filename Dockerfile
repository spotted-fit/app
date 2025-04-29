FROM --platform=linux/amd64 gradle:jdk17 AS build
ARG BASE_URL
ENV BASE_URL=$BASE_URL
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# If .env file exists, extract BASE_URL from it (if not provided as build arg)
RUN if [ -z "$BASE_URL" ] && [ -f .env ]; then \
        export BASE_URL=$(grep BASE_URL .env | cut -d '=' -f2); \
        echo "Using BASE_URL from .env: $BASE_URL"; \
    else \
        echo "Using provided BASE_URL: $BASE_URL"; \
    fi && \
    ./gradlew generateBuildKonfig && \
    ./gradlew wasmJsBrowserDevelopmentExecutableDistribution --no-daemon -PBASE_URL=$BASE_URL

FROM --platform=linux/amd64 nginx:alpine
COPY --from=build /home/gradle/src/composeApp/build/dist/wasmJs/developmentExecutable/ /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
