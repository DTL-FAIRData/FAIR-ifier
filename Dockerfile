FROM openjdk:8-alpine

RUN apk --no-cache add bash wget

COPY . /app/fairifier
WORKDIR /app/fairifier
RUN ./refine build

EXPOSE 3333
CMD cp -f /configuration/config.xml /app/fairifier/extensions/grefine-rdf-extension/config.xml || true && ./refine -i 0.0.0.0 -x refine.headless=true