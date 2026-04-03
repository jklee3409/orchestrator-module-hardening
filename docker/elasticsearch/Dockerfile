ARG ELASTIC_VERSION=8.13.4
FROM docker.elastic.co/elasticsearch/elasticsearch:${ELASTIC_VERSION}

RUN elasticsearch-plugin install --batch analysis-nori
