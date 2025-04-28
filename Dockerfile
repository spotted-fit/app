FROM nginx:alpine

COPY composeApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]