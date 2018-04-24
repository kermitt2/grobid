#!/bin/bash

set -e

commit=$(git rev-parse HEAD)
branch=$(git rev-parse --abbrev-ref HEAD)
echo "commit: $commit ($branch)"

if [ -z "$DOCKER_USERNAME" ]; then
  DOCKER_USERNAME=lfoppiano
fi

IMAGE_NAME=$DOCKER_USERNAME/grobid

build() {
  docker build -t $IMAGE_NAME:$branch .
}

start() {
  docker run -p 8070:8070 $IMAGE_NAME:$branch
}

push() {
  if [ -z "$DOCKER_USERNAME" ] || [ -z "$DOCKER_PASSWORD" ]; then
    echo "DOCKER_USERNAME and DOCKER_PASSWORD environment variables required"
    exit 2
  fi

  docker tag $commit $IMAGE_NAME

  echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
  docker push $IMAGE_NAME:latest
}

case "$1" in 
    build)   build ;;
    start)   start ;;
    push)    push ;;
    *) echo "usage: $0 build|start|push" >&2
       exit 1
       ;;
esac
