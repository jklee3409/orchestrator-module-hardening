#!/bin/bash

echo "--------------- 서버 배포 시작 -----------------"
docker stop capstone-server || true
docker rm capstone-server || true
docker pull 221082211696.dkr.ecr.ap-northeast-2.amazonaws.com/capstone-server/capstone-server:latest
docker run -d --name capstone-server -p 8080:8080 221082211696.dkr.ecr.ap-northeast-2.amazonaws.com/capstone-server/capstone-server:latest
echo "--------------- 서버 배포 끝 -----------------"