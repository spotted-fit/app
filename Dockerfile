FROM --platform=linux/amd64 gradle:jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle wasmJsBrowserDistribution --no-daemon

FROM --platform=linux/amd64 nginx:alpine
COPY --from=build /home/gradle/src/composeApp/build/dist/wasmJs/productionExecutable/ /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]