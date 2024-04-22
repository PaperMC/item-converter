FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache dumb-init

ARG VERSION=0.0.1-SNAPSHOT

COPY build/libs/item-converter-${VERSION}-fat.jar /app/item-converter.jar

WORKDIR /app

ENTRYPOINT ["dumb-init", "--"]
CMD ["java", "-jar", "item-converter.jar"]
