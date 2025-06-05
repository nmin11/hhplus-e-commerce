#!/bin/bash

IMAGE_NAME="hhplus-e-commerce-image"
CONTAINER_NAME="hhplus-e-commerce"
NETWORK_NAME="hhplus-e-commerce_hhplus-net"

docker build -t $IMAGE_NAME .
docker rm -f $CONTAINER_NAME 2>/dev/null || true
docker run --rm -p 8080:8080 \
  --name $CONTAINER_NAME \
  --network $NETWORK_NAME \
  --cpus="4.0" \
  --memory="10g" \
  -e JAVA_OPTS="-Xms1024m -Xmx8192m" \
  -e SPRING_PROFILES_ACTIVE=dev \
  $IMAGE_NAME
